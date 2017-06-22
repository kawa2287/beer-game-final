package mkawa.okhttp;

import android.os.Build;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;


public  class DropToken extends Sprite {

    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------

    private Body body;
    private int nInstances;

    private boolean canRun = false;

    private int footContacts = 0;
    private Vector2 toMovePosition;

    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------

    public DropToken(float pX, float pY, float pXf, float pYf, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, ITextureRegion type, String userData)
    {
        super(pX, pY, pXf, pYf, type , vbo);
        createPhysics(camera, physicsWorld, userData);
        camera.setChaseEntity(this);
    }

    // ---------------------------------------------
    // CLASS LOGIC
    // ---------------------------------------------

    private void createPhysics(final Camera camera, PhysicsWorld physicsWorld, String userData)
    {
        body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyDef.BodyType.DynamicBody, PhysicsFactory.createFixtureDef(.5f,.5f,0.1f));

        body.setUserData(userData);
        body.setFixedRotation(false);


        physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true)
        {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
                camera.onUpdate(0.1f);

            }
        });
    }

    public void jump()
    {
        if (footContacts < 1)
        {
            return;
        }
        body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 12));
    }

    public void increaseFootContacts()
    {
        footContacts++;
    }

    public void decreaseFootContacts()
    {
        footContacts--;
    }

    public void startFall()
    {
        body.setType(BodyDef.BodyType.DynamicBody);
    }




    public void setToMovePosition(float px, float py){
        this.toMovePosition = new Vector2(px, py);
    }

    public Vector2 getToMovePosition(){
        return this.toMovePosition;
    }

    public int getnInstances() {
        return nInstances;
    }
}


