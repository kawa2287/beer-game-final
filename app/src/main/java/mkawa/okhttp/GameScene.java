package mkawa.okhttp;



import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static mkawa.okhttp.TradeShare.ABV_POINT_KEY;
import static mkawa.okhttp.TradeShare.DRINK_POINT_KEY;
import static mkawa.okhttp.TradeShare.FORM_DATA_TYPE;
import static mkawa.okhttp.TradeShare.IBU_POINT_KEY;
import static mkawa.okhttp.TradeShare.NAME_KEY;
import static mkawa.okhttp.TradeShare.OZ_POINT_KEY;
import static mkawa.okhttp.TradeShare.TEAM_KEY;
import static mkawa.okhttp.TradeShare.URL;
import static mkawa.okhttp.TradeShare.playerName;
import static mkawa.okhttp.TradeShare.playerTeam;


public class GameScene extends BaseScene implements IOnSceneTouchListener {

    private HUD gameHUD;
    private Text scoreText;
    private Text dropZoneText;
    private int score = 0;
    private PhysicsWorld physicsWorld;
    private DropToken player;
    private Text gameOverText;
    private Text drinkPointText;
    private Text ozPointText;
    private Text abvPointText;
    private Text ibuPointText;
    private boolean gameOverDisplayed = false;
    private Rectangle endRectangle;

    private int drinkTokens = TradeShare.getDrinkTokens();
    private int ozTokens = TradeShare.getOzTokens();
    private int abvTokens = TradeShare.getAbvTokens();
    private int ibuTokens = TradeShare.getIbuTokens();
    private int nTokens = TradeShare.getNumInstances();

    private int drinkPoints;
    private int ozPoints;
    private int abvPoints;
    private int ibuPoints;

    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";

    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WALL = "wall";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_CONTAINERWALL = "containerWall";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR = "floor";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_POINTREGION = "pointRegion";

    private int [] spotList = new int[12];

    @Override
    public void createScene()
    {
        createBackground();
        createHUD();
        createPhysics();
        loadLevel(1);
        createGameOverText();
        setOnSceneTouchListener(this);

        //CREATE POINT DISTRIBUTION ARRAY
        int [] spotListTemp = new int[12];
        ArrayList<Integer> usedList = new ArrayList<>();
        ArrayList<Integer> pointsList = new ArrayList<>();
        pointsList.add(3);
        pointsList.add(2);
        pointsList.add(2);
        pointsList.add(1);
        pointsList.add(1);
        pointsList.add(1);
        pointsList.add(1);
        pointsList.add(1);
        pointsList.add(0);
        pointsList.add(0);
        pointsList.add(0);
        pointsList.add(0);

        for (int i = 0 ; i <= 11; i++)
        {
            System.out.println("pointPos " + i+ " = " + pointsList.get(i));
        }


        // CREATE RECTANGLES AND ASSOCIATE POINTS
        FixtureDef POINT_FIXTURE = PhysicsFactory.createFixtureDef(0f,0f,0f);
        POINT_FIXTURE.isSensor = true;
        final ArrayList<Rectangle> rectTemp = new ArrayList<>();
        for(int i = 1; i <=12;i++)
        {
            rectTemp.add((new Rectangle((i * 40f) - 20f,10f,20f,10f,vbom)));
            int foundSpot = 0;
            int spot = (int)Math.floor(Math.random()*(11-0+1)+0);

            if ( usedList.size() == 0)
            {
                usedList.add(spot);
                spotListTemp[i-1] = pointsList.get(spot);
                PhysicsFactory.createBoxBody(physicsWorld,rectTemp.get(i-1), BodyDef.BodyType.StaticBody,POINT_FIXTURE).setUserData(String.valueOf(pointsList.get(spot)));

                //Set Color
                switch (pointsList.get(spot))
                {
                    case 0: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.IndianRed));
                        break;
                    case 1: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.WhiteSmoke));
                        break;
                    case 2: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.DodgerBlue));
                        break;
                    case 3: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.Goldenrod));
                        break;
                }
                attachChild(rectTemp.get(i-1));
            }
            else
            {
                while (foundSpot == 0)
                {
                    int foundDup = 0;
                    for (int k = 0; k < usedList.size(); k++)
                    {
                        if (usedList.get(k) == spot)
                        {
                            foundDup = 1;
                            break;
                        }
                    }

                    if ( foundDup == 1)
                    {
                        spot = (int)Math.floor(Math.random()*(11-0+1)+0);
                    }
                    else
                    {
                        foundSpot = 1;
                        usedList.add(spot);
                        spotListTemp[i-1] = pointsList.get(spot);
                        PhysicsFactory.createBoxBody(physicsWorld,rectTemp.get(i-1), BodyDef.BodyType.StaticBody,POINT_FIXTURE).setUserData(String.valueOf(pointsList.get(spot)));
                        //Set Color
                        switch (pointsList.get(spot)){
                            case 0: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.IndianRed));
                                break;
                            case 1: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.WhiteSmoke));
                                break;
                            case 2: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.DodgerBlue));
                                break;
                            case 3: rectTemp.get(i-1).setColor(ContextCompat.getColor(activity.getApplicationContext(),R.color.Goldenrod));
                                break;
                        }
                        attachChild(rectTemp.get(i-1));
                    }
                }
            }
        }


        //CREATE OBSTACLES
        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        ArrayList<Sprite> spriteGen = new ArrayList<>();
        for (int i = 1; i<=12; i++)
        {
            float elev = (float)Math.floor(Math.random()*(690-110+1)+110);
            spriteGen.add(new Sprite((i * 40)-20,elev,20,20,resourcesManager.platform1_region,vbom));
            PhysicsFactory.createCircleBody(physicsWorld, spriteGen.get(i-1), BodyDef.BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
            attachChild(spriteGen.get(i-1));
        }

        //CREATE DROP ZONE
        Rectangle dropZone = new Rectangle(240f,750f,470f,90f,vbom);
        dropZone.setColor(255/255,255/255,255/255,0.2f);
        attachChild(dropZone);

        spotList = spotListTemp;

        //CREATE POINT TEXTS
        ArrayList<Text> textList = new ArrayList<>();
        for (int i = 1; i <= 12; i++)
        {
            textList.add(new Text((i * 40)-20,30, resourcesManager.font, String.valueOf(spotList[i-1]), new TextOptions(HorizontalAlign.CENTER), vbom));
            gameHUD.attachChild(textList.get(i-1));
        }



    }

    @Override
    public void onBackKeyPressed()
    {

    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        camera.setChaseEntity(null);

    }

    private void createBackground()
    {
        setBackground(new Background(50f/255,50f/255,50f/255));
    }

    private void createHUD()
    {
        gameHUD = new HUD();

        // CREATE SCORE TEXT
        scoreText = new Text(20, 640, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        scoreText.setAnchorCenter(0, 0);
        scoreText.setText("Score: 0");
        gameHUD.attachChild(scoreText);

        //CREATE DROP ZONE TEXT
        dropZoneText = new Text(240,750, resourcesManager.font, "DROP ZONE", new TextOptions(HorizontalAlign.CENTER), vbom);
        gameHUD.attachChild(dropZoneText);

        camera.setHUD(gameHUD);
    }

    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }

    private void createPhysics()
    {
        physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -20), false);
        physicsWorld.setContactListener(contactListener());
        registerUpdateHandler(physicsWorld);
    }


    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
    {
        if (pSceneTouchEvent.isActionDown())
        {
            if(pSceneTouchEvent.getY() >= 700f)
            {
                if( drinkTokens > 0 )
                {
                    player = new DropToken(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 30f, 30f, vbom, camera, physicsWorld, ResourcesManager.getInstance().spade_region, "drink token");
                    attachChild(player);
                    drinkTokens = drinkTokens - 1;
                    return true;
                }
                else if (ozTokens > 0)
                {
                    player = new DropToken(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 30f, 30f, vbom, camera, physicsWorld, ResourcesManager.getInstance().diamond_region, "oz token");
                    attachChild(player);
                    ozTokens = ozTokens - 1;
                    return true;
                }
                else if (abvTokens > 0)
                {
                    player = new DropToken(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 30f, 30f, vbom, camera, physicsWorld, ResourcesManager.getInstance().heart_region, "abv token");
                    attachChild(player);
                    abvTokens = abvTokens - 1;
                    return true;
                }
                else if (ibuTokens > 0)
                {
                    player = new DropToken(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 30f, 30f, vbom, camera, physicsWorld, ResourcesManager.getInstance().club_region, "ibu token");
                    attachChild(player);
                    ibuTokens = ibuTokens - 1;
                    return true;
                }
            }
        }
        else if (pSceneTouchEvent.isActionUp())
        {
           // player.startFall();
        }

        return false;
    }



    private void loadLevel(int levelID)
    {
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);

        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);

        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);

                camera.setBounds(0, 0, width, height); // here we set camera bounds
                camera.setBoundsEnabled(true);

                return GameScene.this;
            }
        });

        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);

                final Sprite levelObject;

                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WALL))
                {
                    levelObject = new Sprite(x, y, 1, 800, resourcesManager.wall_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("wall");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_CONTAINERWALL))
                {
                    levelObject = new Sprite(x, y, 1, 120, resourcesManager.wall_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("containerWall");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR))
                {
                    levelObject = new Sprite(x, y, 480, 1, resourcesManager.wall_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("floor");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_POINTREGION))
                {
                    levelObject = new Sprite(x, y, 80, 120, resourcesManager.point_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("region_A");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                levelObject.setCullingEnabled(true);

                return levelObject;
            }
        });
        levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
    }

    private void createGameOverText()
    {
        gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
        drinkPointText = new Text(0, 0, resourcesManager.font, "drink pts = " + String.valueOf(drinkPoints),vbom);
        ozPointText = new Text(0, 0, resourcesManager.font, "oz pts = " + String.valueOf(ozPoints),vbom);
        abvPointText = new Text(0, 0, resourcesManager.font, "abv pts = " + String.valueOf(abvPoints),vbom);
        ibuPointText = new Text(0, 0, resourcesManager.font, "ibu pts = " + String.valueOf(ibuPoints),vbom);

        endRectangle = new Rectangle(0,0,300,400,vbom){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
            {
                if (pSceneTouchEvent.isActionDown())
                {
                    activity.startActivity(new Intent(activity,Dashboard.class));
                    activity.finish();
                    return true;
                }
                return false;
            }
        };
        endRectangle.setColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.Black));
        endRectangle.setAlpha(0.8f);

        registerTouchArea(endRectangle);
        setTouchAreaBindingOnActionDownEnabled(true);
    }

    private void displayGameOverText()
    {
        camera.setChaseEntity(null);
        gameOverText.setPosition(camera.getCenterX(), camera.getCenterY()+100);
        drinkPointText.setPosition(camera.getCenterX(), camera.getCenterY()+50);
        ozPointText.setPosition(camera.getCenterX(), camera.getCenterY());
        abvPointText.setPosition(camera.getCenterX(), camera.getCenterY()-50);
        ibuPointText.setPosition(camera.getCenterX(), camera.getCenterY()-100);
        endRectangle.setPosition(camera.getCenterX(), camera.getCenterY());

        attachChild(endRectangle);
        attachChild(gameOverText);
        attachChild(drinkPointText);
        attachChild(ozPointText);
        attachChild(abvPointText);
        attachChild(ibuPointText);

        gameOverDisplayed = true;
    }



    private ContactListener contactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if(x2.getBody().getUserData().toString().contains("token") && x1.getBody().getUserData().equals("floor"))
                    {
                        x2.setRestitution(.7f);

                    }
                    else if (x2.getBody().getUserData().toString().contains("token")&& x1.getBody().getUserData().equals("0"))
                    {
                        nTokens = nTokens - 1;
                    }
                    else if (x2.getBody().getUserData().toString().contains("token")&& x1.getBody().getUserData().equals("1"))
                    {
                        if(x2.getBody().getUserData().toString().contains("drink")){
                            drinkPoints = drinkPoints + 1;
                            addToScore(1);
                        } else if (x2.getBody().getUserData().toString().contains("oz")){
                            ozPoints = ozPoints + 1;
                            addToScore(1);
                        } else if (x2.getBody().getUserData().toString().contains("abv")){
                            abvPoints = abvPoints + 1;
                            addToScore(1);
                        } else if (x2.getBody().getUserData().toString().contains("ibu")){
                            ibuPoints = ibuPoints + 1;
                            addToScore(1);
                        }
                        nTokens = nTokens - 1;
                    }
                    else if (x2.getBody().getUserData().toString().contains("token")& x1.getBody().getUserData().equals("2"))
                    {
                        if(x2.getBody().getUserData().toString().contains("drink")){
                            drinkPoints = drinkPoints + 2;
                            addToScore(2);
                        } else if (x2.getBody().getUserData().toString().contains("oz")){
                            ozPoints = ozPoints + 2;
                            addToScore(2);
                        } else if (x2.getBody().getUserData().toString().contains("abv")){
                            abvPoints = abvPoints + 2;
                            addToScore(2);
                        } else if (x2.getBody().getUserData().toString().contains("ibu")){
                            ibuPoints = ibuPoints + 2;
                            addToScore(2);
                        }
                        nTokens = nTokens - 1;
                    }
                    else if (x2.getBody().getUserData().toString().contains("token")&& x1.getBody().getUserData().equals("3"))
                    {
                        if(x2.getBody().getUserData().toString().contains("drink")){
                            drinkPoints = drinkPoints + 3;
                            addToScore(3);
                        } else if (x2.getBody().getUserData().toString().contains("oz")){
                            ozPoints = ozPoints + 3;
                            addToScore(3);
                        } else if (x2.getBody().getUserData().toString().contains("abv")){
                            abvPoints = abvPoints + 3;
                            addToScore(3);
                        } else if (x2.getBody().getUserData().toString().contains("ibu")){
                            ibuPoints = ibuPoints + 3;
                            addToScore(3);
                        }
                        nTokens = nTokens - 1;
                    }
                    else if (x2.getBody().getUserData().toString().contains("token"))
                    {
                        player.increaseFootContacts();
                        x2.setRestitution(.9f);
                        x1.setFriction(0.1f);
                        x2.setFriction(0.1f);

                    }
                }
            }

            public void endContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if (x2.getBody().getUserData().toString().contains("token") && (
                            (x1.getBody().getUserData().equals("0")) ||
                            (x1.getBody().getUserData().equals("1")) ||
                            (x1.getBody().getUserData().equals("2")) ||
                            (x1.getBody().getUserData().equals("3"))))
                    {
                        if (nTokens == 0){
                            createGameOverText();
                            displayGameOverText();
                            executeTrade();
                        }
                    }
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold)
            {

            }

            public void postSolve(Contact contact, ContactImpulse impulse)
            {

            }
        };
        return contactListener;
    }

    public void executeTrade ()
    {
        //code here to submit token points

        //Create an object for PostDataTask AsyncTask
        GamePoints gamePoints = new GamePoints();
        gamePoints.setDrinkPoints(String.valueOf(drinkPoints));
        gamePoints.setOzPoints(String.valueOf(ozPoints));
        gamePoints.setAbvPoints(String.valueOf(abvPoints));
        gamePoints.setIbuPoints(String.valueOf(ibuPoints));
        gamePoints.setURL(URL);
        gamePoints.setTest(true);
        gamePoints.setPlayerName(playerName);
        gamePoints.setTeamName(playerTeam);

        //Create an object for PostDataTask AsyncTask
        PostDataTask postDataTask = new PostDataTask();

        postDataTask.execute(gamePoints);
    }


    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<GamePoints, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(GamePoints... gamePoints)
        {
            Boolean result = gamePoints[0].getTest();
            String name = gamePoints[0].getPlayerName();
            String postBody="";
            String URL = gamePoints[0].getURL();
            String drinkP = gamePoints[0].getDrinkPoints();
            String ozP = gamePoints[0].getOzPoints();
            String abvP = gamePoints[0].getAbvPoints();
            String ibuP = gamePoints[0].getIbuPoints();
            String team = gamePoints[0].getTeamName();

            try
            {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = NAME_KEY+"=" + URLEncoder.encode(name,"UTF-8") +
                        "&" + DRINK_POINT_KEY + "=" + URLEncoder.encode(drinkP,"UTF-8") +
                        "&" + OZ_POINT_KEY + "=" + URLEncoder.encode(ozP,"UTF-8") +
                        "&" + ABV_POINT_KEY + "=" + URLEncoder.encode(abvP,"UTF-8") +
                        "&" + IBU_POINT_KEY + "=" + URLEncoder.encode(ibuP,"UTF-8") +
                        "&" + TEAM_KEY + "=" + URLEncoder.encode(team,"UTF-8");

            }
            catch (UnsupportedEncodingException ex)
            {
                result = false;
            }

            try
            {
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();
                //Send the request
                Response response = client.newCall(request).execute();
            }
            catch (IOException exception)
            {
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean w){
            //Print Success or failure message accordingly
            final Boolean test = w;


            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(activity.getApplicationContext(),test?"Points Submitted":"There was some error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
