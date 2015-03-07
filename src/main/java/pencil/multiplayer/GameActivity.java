package pencil.multiplayer;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.graphics.Typeface;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;


/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 11:54:51 - 03.04.2010
 */
public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener{
	// ===========================================================
	// Constants
	// ===========================================================

	static final int CAMERA_WIDTH = 960;
	static final int CAMERA_HEIGHT = 540;
	
	static float screen_touchX = 0;
	static float screen_touchY = 0;
	private static Scene scene;
	public static int scoreCount = 0, opponentScoreCount = 0;

	// ===========================================================
	// Fields
	// ===========================================================

	private ITexture mTexture;
	private static PhysicsWorld mPhysicsWorld;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BuildableBitmapTextureAtlas longBitmapTextureAtlas;
	private static VertexBufferObjectManager vertexBufferObjectManager;
	private static ITextureRegion fingerTextureRegion;
	private static TiledTextureRegion basketTexture;
	static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private static final float FONT_SIZE_MEDIUM = 10;
	static List<Ball> balls = new ArrayList<Ball>();
	Basket basket;
	private Font mFont;
	private float finger_touchX;
	private float finger_touchY;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		try {
			this.mTexture = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return getAssets().open("sharp.png");
				}
			});

			this.mTexture.load();
			this.fingerTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		} catch (IOException e) {
			Debug.e(e);
		}
		
		this.longBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.NEAREST);
		// load the bird texture
		basketTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.longBitmapTextureAtlas, this,
				"football.png", 1, 1);

		try {		
			this.longBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			this.longBitmapTextureAtlas.load();
			
			
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		// load fonts
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
		this.mFont.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		vertexBufferObjectManager =  this.getVertexBufferObjectManager();

		scene = new Scene();
		scene.setBackground(new Background(1,1,1));
		scene.setOnSceneTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		scene.registerUpdateHandler(this.mPhysicsWorld);
	
		
		basket = new Basket(CAMERA_WIDTH/2 - Basket.width /2, CAMERA_HEIGHT/2, vertexBufferObjectManager, basketTexture, mPhysicsWorld, scene);
		int maxTextLength = 200;
		final Text scoreText = new Text(CAMERA_WIDTH * 0.5f, CAMERA_HEIGHT * 0.1f, mFont, "Me : "+scoreCount + "... Opponent : " + opponentScoreCount, maxTextLength, getVertexBufferObjectManager());
		scene.attachChild(scoreText);
		
		
		mPhysicsWorld.setContactListener(new ContactListener() {
    		@Override
			public void beginContact(Contact contact) {
    			 final Fixture x1 = contact.getFixtureA();
    	         final Fixture x2 = contact.getFixtureB();
    	         if (x2.getBody().getUserData().equals("ball")&&x1.getBody().getUserData().equals("basketInner"))
    	         {
    	            basket.base.setColor(Color.RED);
    	            scoreCount++;
    	            x2.getBody().setUserData("collidedBall");
    	            scoreText.setText("Me : "+scoreCount + "... Opponent : " + opponentScoreCount);
    	         }
    	         if (x2.getBody().getUserData().equals("opponentBall")&&x1.getBody().getUserData().equals("basketInner"))
    	         {
    	            basket.base.setColor(Color.BLUE);
    	            opponentScoreCount++;
    	            x2.getBody().setUserData("collidedOpponentBall");
    	            scoreText.setText("Me : "+scoreCount + "... Opponent : " + opponentScoreCount);
    	         }
    	         if(scoreCount  > 2) {
    	        	scoreText.setText("You Won the game !!!");
    	        	 mEngine.stop();
    	         }
    	         else if (opponentScoreCount > 2) {
    	        	 scoreText.setText("You are the LOSER :p");
    	        	 mEngine.stop(); 
    	         }
    	         
			}

			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
				
			}
    });

		//Ball ball = new Ball(200, 0, vertexBufferObjectManager, basketTexture, mPhysicsWorld, scene);
	/*	scene.registerUpdateHandler(new IUpdateHandler() {			 
			public void reset() {
			}
			// main game loop
			public void onUpdate(float pSecondsElapsed) {
				for(int i = 0; i < balls.size(); i++) {
					//if(balls.get(i).ballBody.
				}
			}
		});		*/	
		return scene;
	}
	
	public static void opponentEvent(float x, float y) {
		float pos_x = 0, pos_y = 0, touchX = 0, touchY = 0;
		// opponent is right player
		if(MainActivity.isLeftPlayer) {
			pos_x = CAMERA_WIDTH;
			pos_y = CAMERA_HEIGHT;
			touchX = x-CAMERA_WIDTH;
			touchY = y;
		}
		// opponent is left player
		else {
			pos_x = 0;
			pos_y = CAMERA_HEIGHT;
			touchX = x;
			touchY = y;
		}
		Ball opponentBall = new Ball(pos_x, pos_y, vertexBufferObjectManager, basketTexture,mPhysicsWorld, scene, touchX, touchY);
		opponentBall.ballBody.setUserData("opponentBall");
		opponentBall.ballSprite.setColor(1, 0, 0);
		balls.add(opponentBall);
		
		
		/*final Sprite finger = new Sprite(x , y , fingerTextureRegion, vertexBufferObjectManager);
		finger.setColor((float)Math.random(), (float)Math.random(), (float)Math.random());
		scene.attachChild(finger);*/	
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		
		
		if (pSceneTouchEvent.isActionDown()) {
			screen_touchX = pSceneTouchEvent.getX();
			screen_touchY = pSceneTouchEvent.getY()-CAMERA_HEIGHT;
			
			float pos_x = 0, pos_y = 0, touchX = 0, touchY = 0;
			if(MainActivity.isLeftPlayer) {
				pos_x = 0;
				pos_y = CAMERA_HEIGHT;
				touchX = screen_touchX;
				touchY = screen_touchY;
			}
			else {
				pos_x = CAMERA_WIDTH;
				pos_y = CAMERA_HEIGHT;
				touchX = screen_touchX-CAMERA_WIDTH;
				touchY = screen_touchY;				
			}
			
			MainActivity.broadcastBall(screen_touchX, screen_touchY);
			
			balls.add(new Ball(pos_x, pos_y, vertexBufferObjectManager, basketTexture,mPhysicsWorld, scene,
					touchX, touchY));				
		
			return true;
		}
		
		/*if (pSceneTouchEvent.isActionDown()) {
			finger_touchX = pSceneTouchEvent.getX() - fingerTextureRegion.getWidth()/2;
			finger_touchY = pSceneTouchEvent.getY() - fingerTextureRegion.getHeight()/2;
			final Sprite fingerPrint = new Sprite(finger_touchX , finger_touchY , fingerTextureRegion, this.getVertexBufferObjectManager());
			fingerPrint.setColor((float)Math.random(), (float)Math.random(), (float)Math.random());			
			float pressure = pSceneTouchEvent.getMotionEvent().getSize();
			fingerPrint.setScale(pressure);
			scene.attachChild(fingerPrint);
			MainActivity.broadcastPrint(finger_touchX, finger_touchY);
			return true;
		}*/
		return false;
	}
	@Override
	public final void onPause() {
		super.onPause();	
		this.mEngine.stop();	
	}

	@Override
	public final void onResume() {
		super.onResume();	
		this.mEngine.start();	
	}	
	
	//@Override
	/*    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
        	this.startActivity(new Intent(this, MainActivity.class)); 
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }*/
}
