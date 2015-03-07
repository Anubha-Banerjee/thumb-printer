package pencil.multiplayer;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;


class Basket{
	float positionX, positionY;	
	static float height = GameActivity.CAMERA_HEIGHT/12, width =  GameActivity.CAMERA_WIDTH/7;
	static final int angle = GameActivity.CAMERA_WIDTH/18;
	AnimatedSprite basketSprite;
	Line base, left, right, inner;
	int waterLevel = 2;
	public Basket(float x, float y, final VertexBufferObjectManager vertexBufferObjectManager, TiledTextureRegion basketTexture, PhysicsWorld physicsWorld, Scene scene) {
		
		//spriteImage.animate(100);
		basketSprite = new AnimatedSprite(x, y, basketTexture, vertexBufferObjectManager);
	
		setPositionX(x);
		setPositionY(y);
		base = new Line(positionX, positionY, positionX + width, positionY, vertexBufferObjectManager);
		left = new Line(positionX, positionY, positionX - angle, positionY - height, vertexBufferObjectManager);
		right = new Line(positionX + width, positionY, positionX + width + angle, positionY - height, vertexBufferObjectManager);
		inner = new Line(positionX, positionY-waterLevel, positionX + width, positionY-waterLevel, vertexBufferObjectManager);
	
		base.setColor(Color.BLACK);
		base.setLineWidth(5);
		left.setColor(Color.BLACK);
		left.setLineWidth(5);
		right.setColor(Color.BLACK);
		right.setLineWidth(5);
		inner.setColor(Color.BLUE);
		inner.setLineWidth(1);
		
		//Basket basket = new Basket(100, 100, vertexBufferObjectManager,basketTexture, mPhysicsWorld, scene);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.2f, 0);
		Body baseBody = PhysicsFactory.createLineBody(physicsWorld, base, wallFixtureDef);
		Body innerBody = PhysicsFactory.createLineBody(physicsWorld, inner, wallFixtureDef);
		Body leftBody = PhysicsFactory.createLineBody(physicsWorld, left, wallFixtureDef);
		Body rightBody  = PhysicsFactory.createLineBody(physicsWorld, right, wallFixtureDef);
		baseBody.setUserData("basketBase");
		leftBody.setUserData("basketLeft");
		rightBody.setUserData("basketRight");
		innerBody.setUserData("basketInner");
		scene.attachChild(base);	
		scene.attachChild(left);
		scene.attachChild(right);
		
	}
	public void setPositionX(float x) {
		positionX = x;
	}
	public void setPositionY(float y) {
		positionY = y;
	}
}
