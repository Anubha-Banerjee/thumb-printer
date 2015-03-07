package pencil.multiplayer;

import java.nio.channels.GatheringByteChannel;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;


class Ball{
	float positionX, positionY;	
	float height, width;
	AnimatedSprite ballSprite;
	Body ballBody;
	public Ball(float x, float y, final VertexBufferObjectManager vertexBufferObjectManager, TiledTextureRegion ballTexture, PhysicsWorld physicsWorld, Scene scene,
			float shootX, float shootY) {
		
		//spriteImage.animate(100);
		ballSprite = new AnimatedSprite(x, y, ballTexture, vertexBufferObjectManager);		
		ballBody = PhysicsFactory.createCircleBody(physicsWorld, ballSprite, BodyType.DynamicBody, GameActivity.FIXTURE_DEF);
		scene.attachChild(ballSprite);
		ballBody.setUserData("ball");
		//ballSprite.setScale(0.2f);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(ballSprite, ballBody, true, true));
		shootBall(shootX, shootY);
	}
	
	private void shootBall(float x, float y) {		
		ballBody.setLinearVelocity(new Vector2(x / 20, y/ 20));
	}
	
}
