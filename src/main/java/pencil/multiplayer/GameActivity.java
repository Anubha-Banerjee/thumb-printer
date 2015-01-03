package pencil.multiplayer;


import java.io.IOException;
import java.io.InputStream;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;


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

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	private static float finger_touchX = 0;
	private static float finger_touchY = 0;
	private static Scene scene;

	// ===========================================================
	// Fields
	// ===========================================================

	private ITexture mTexture;
	private static VertexBufferObjectManager vertexBufferObjectManager;
	private static ITextureRegion fingerTextureRegion;

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
					return getAssets().open("thumb_black.png");
				}
			});

			this.mTexture.load();
			this.fingerTextureRegion = TextureRegionFactory.extractFromTexture(this.mTexture);
		} catch (IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		vertexBufferObjectManager =  mEngine.getVertexBufferObjectManager();

		scene = new Scene();
		scene.setBackground(new Background(1,1,1));
		scene.setOnSceneTouchListener(this);
		return scene;
	}
	public static void opponentTouchEvent(float x, float y) {
		final Sprite finger = new Sprite(x , y , fingerTextureRegion, vertexBufferObjectManager);
		finger.setColor((float)Math.random(), (float)Math.random(), (float)Math.random());
		scene.attachChild(finger);		
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		
		if (pSceneTouchEvent.isActionDown()) {
			finger_touchX = pSceneTouchEvent.getX() - fingerTextureRegion.getWidth()/2;
			finger_touchY = pSceneTouchEvent.getY() - fingerTextureRegion.getHeight()/2;
			/* Create the face and add it to the scene. */
			final Sprite face = new Sprite(finger_touchX , finger_touchY , fingerTextureRegion, this.getVertexBufferObjectManager());
			face.setColor((float)Math.random(), (float)Math.random(), (float)Math.random());
			scene.attachChild(face);
			MainActivity.broadcastPrint(finger_touchX, finger_touchY);
			return true;
		}
		return false;
	}	// ===========================================================
}
