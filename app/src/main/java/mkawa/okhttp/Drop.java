package mkawa.okhttp;


        import android.os.Bundle;
        import android.view.KeyEvent;
        import android.widget.ImageView;

        import org.andengine.engine.Engine;
        import org.andengine.engine.LimitedFPSEngine;
        import org.andengine.engine.camera.BoundCamera;
        import org.andengine.engine.camera.Camera;
        import org.andengine.engine.handler.timer.ITimerCallback;
        import org.andengine.engine.handler.timer.TimerHandler;
        import org.andengine.engine.options.EngineOptions;
        import org.andengine.engine.options.ScreenOrientation;
        import org.andengine.engine.options.WakeLockOptions;
        import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
        import org.andengine.entity.scene.Scene;
        import java.io.IOException;
        import java.util.ArrayList;

        import org.andengine.ui.activity.BaseGameActivity;

public class Drop extends BaseGameActivity
{
    private BoundCamera camera = new BoundCamera(0, 0, 800, 480);
    private ResourcesManager resourcesManager;
    public int CAMERA_WIDTH = 480;
    public int CAMERA_HEIGHT = 800;

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException
    {
        ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
        resourcesManager = ResourcesManager.getInstance();
        pOnCreateResourcesCallback.onCreateResourcesFinished();

    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException {
        SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);

    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException {
        mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback()
        {
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                SceneManager.getInstance().createGameScene();

            }
        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();

    }

    @Override
    public EngineOptions onCreateEngineOptions()
    {
        camera = new BoundCamera(0, 0, 480, 800);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        return engineOptions;
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions)
    {
        return new LimitedFPSEngine(pEngineOptions, 30);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        System.exit(0);
    }

    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
        }
        return false;
    }
    */
}