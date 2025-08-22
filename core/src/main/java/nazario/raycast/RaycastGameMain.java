package nazario.raycast;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class RaycastGameMain extends ApplicationAdapter {
    private static final float ONE_STEP = 0.017453292519943295f;

    private ShapeRenderer renderer;
    private int windowWidth, windowHeight;

    float playerX = 300, playerY = 300, playerDeltaX = 0, playerDeltaY = 0, playerAngle = 0;

    int[] map = {
        1, 1, 1, 1, 1, 1, 1, 1,
        1, 0, 1, 0, 0, 0, 0, 1,
        1, 0, 1, 0, 0, 0, 0, 1,
        1, 0, 1, 0, 0, 0, 0, 1,
        1, 0, 0, 0, 0, 0, 0, 1,
        1, 0, 0, 0, 0, 1, 0, 1,
        1, 0, 0, 0, 0, 0, 0, 1,
        1, 1, 1, 1, 1, 1, 1, 1,
    };
    int mapWidth = 8, mapHeight = 8, mapSize = map.length;


    @Override
    public void create() {
        renderer = new ShapeRenderer();
        renderer.setAutoShapeType(true);

        this.windowWidth = Gdx.graphics.getWidth();
        this.windowHeight = Gdx.graphics.getHeight();

        playerDeltaX = (float)Math.cos(playerAngle)*5;
        playerDeltaY = (float)Math.sin(playerAngle)*5;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.3f,0.3f,0.3f, 1f);

        renderer.begin();
        renderer.set(ShapeRenderer.ShapeType.Filled);

        renderer.setColor(0.1f, 0.1f, 0.1f, 1);
        renderer.rect(0, 0, this.windowWidth, this.windowHeight/2f);
        renderer.setColor(0.1f, 0.1f, 0.2f, 1);
        renderer.rect(0, this.windowHeight/2f, this.windowWidth, this.windowHeight/2f);

        drawMap();
        drawPlayer();
        movePlayer();

        drawRays();

        renderer.end();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    public void movePlayer() {
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += playerDeltaY;
            playerX += playerDeltaX;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= playerDeltaY;
            playerX -= playerDeltaX;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerAngle -= 0.1f;
            if(playerAngle < 0) playerAngle += 2*(float)Math.PI;
            playerDeltaX = (float)Math.cos(playerAngle)*5;
            playerDeltaY = (float)Math.sin(playerAngle)*5;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerAngle += 0.1f;
            if(playerAngle > 2*Math.PI) playerAngle -= 2*(float)Math.PI;
            playerDeltaX = (float)Math.cos(playerAngle)*5;
            playerDeltaY = (float)Math.sin(playerAngle)*5;
        }
    }

    public void drawPlayer() {
        float playerDrawX, playerDrawY, playerDrawDeltaX, playerDrawDeltaY;

        playerDrawX = this.playerX;
        playerDrawY = this.windowHeight - this.playerY;
        playerDrawDeltaX = ((float)Math.cos(playerAngle)*5);
        playerDrawDeltaY = -((float)Math.sin(playerAngle)*5);

        renderer.setColor(1, 1, 0, 1);
        renderer.rect(playerDrawX, playerDrawY, 8, 8);

        renderer.rectLine(playerDrawX + 4f, playerDrawY + 4f, (playerDrawX + playerDrawDeltaX * 5) + 4, (playerDrawY + playerDrawDeltaY * 5) + 4, 5);
    }

    public void drawMap() {
        for(int y = 0; y < this.mapHeight; y++) {
            for(int x = 0; x < this.mapWidth; x++) {
                if(map[y*mapWidth+x] == 1) {
                    renderer.setColor(1, 1, 1, 1);
                } else {
                    renderer.setColor(0, 0, 0, 1);
                }
                renderer.rect(x * mapSize + 1, this.windowHeight - y * mapSize - mapSize + 1, mapSize - 2, mapSize - 2);
            }
        }
    }

    public void drawRays() {
        int ray = 0, maxX = 0, maxY = 0, mapPosition = 0, depthOfField = 0;
        float rayAngle, rayX = 0, rayY = 0, rayOffsetX = 0, rayOffsetY = 0, finalDistance = 0;

        rayAngle = this.playerAngle - ONE_STEP*35;
        if(rayAngle < 0) {
            rayAngle += 2f*(float)Math.PI;
        }
        if(rayAngle > 2*Math.PI) {
            rayAngle -= 2f*(float)Math.PI;
        }

        for(ray = 0;ray<35*2;ray++) {
            //region -- Check Horizontal Lines --
            float distanceHorizontal = Float.MAX_VALUE;
            float horizontalX = playerX;
            float horizontalY = playerY;

            depthOfField = 0;
            float aTan = -1f/(float)Math.tan(rayAngle);
            if(rayAngle > Math.PI) { // * Looking Upwards
                rayY = (((int)playerY>>6)<<6) - 0.0001f;
                rayX = (playerY - rayY) * aTan+playerX;

                rayOffsetY = -64;
                rayOffsetX = -rayOffsetY*aTan;
            }

            if(rayAngle < Math.PI) { // * Looking Downwards
                rayY = (((int)playerY>>6)<<6) + 64f;
                rayX = (playerY - rayY) * aTan+playerX;

                rayOffsetY = 64;
                rayOffsetX = -rayOffsetY*aTan;
            }

            if(rayAngle == 0 || rayAngle == Math.PI) {
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while(depthOfField<8) {
                maxX = (int)(rayX) >> 6;
                maxY = (int)(rayY) >> 6;
                mapPosition = maxY*mapWidth+maxX;

                if(mapPosition < mapWidth*mapHeight && mapPosition > 0 && map[mapPosition] == 1) { // * Hit Wall
                    depthOfField = 8;

                    horizontalX = rayX;
                    horizontalY = rayY;
                    distanceHorizontal = distance(playerX, playerY, horizontalX, horizontalY, rayAngle);
                } else { // * No Wall Hit
                    rayX += rayOffsetX;
                    rayY += rayOffsetY;
                    depthOfField++;
                }
            }

            //endregion

            //region -- Check Vertical Lines --
            float distanceVertical = Float.MAX_VALUE;
            float verticalX = playerX;
            float verticalY = playerY;

            depthOfField = 0;
            float nTan = -(float)Math.tan(rayAngle);
            if(rayAngle > Math.PI/2f && rayAngle < 3f*Math.PI/2f) { // * Looking Left
                rayX = (((int)playerX>>6)<<6) - 0.0001f;
                rayY = (playerX - rayX) * nTan+playerY;

                rayOffsetX = -64;
                rayOffsetY = -rayOffsetX*nTan;
            }

            if(rayAngle < Math.PI/2f || rayAngle > 3f*Math.PI/2f) { // * Looking Right
                rayX = (((int)playerX>>6)<<6) + 64f;
                rayY = (playerX - rayX) * nTan+playerY;

                rayOffsetX = 64;
                rayOffsetY = -rayOffsetX*nTan;
            }

            if(rayAngle == Math.PI/2f || rayAngle == 3f*Math.PI/2f) {
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while(depthOfField<8) {
                maxX = (int)(rayX) >> 6;
                maxY = (int)(rayY) >> 6;
                mapPosition = maxY*mapWidth+maxX;

                if(mapPosition < mapWidth*mapHeight && mapPosition > 0 && map[mapPosition] == 1) { // * Hit Wall
                    depthOfField = 8;

                    verticalX = rayX;
                    verticalY = rayY;
                    distanceVertical = distance(playerX, playerY, verticalX, verticalY, rayAngle);
                } else { // * No Wall Hit
                    rayX += rayOffsetX;
                    rayY += rayOffsetY;
                    depthOfField++;
                }
            }
            //endregion

            if(distanceVertical < distanceHorizontal) {
                rayX = verticalX;
                rayY = verticalY;

                finalDistance = distanceVertical;
                renderer.setColor(0.8f, 0, 0, 1);
            }
            if(distanceVertical > distanceHorizontal) {
                rayX = horizontalX;
                rayY = horizontalY;

                finalDistance = distanceHorizontal;
                renderer.setColor(1, 0, 0, 1);
            }

            draw3DScene(ray, rayAngle, finalDistance);

            float playerDrawX = this.playerX;
            float playerDrawY = Math.abs(this.windowHeight - this.playerY);
            float rayDrawX = rayX;
            float rayDrawY = Math.abs(this.windowHeight - rayY);
            renderer.setColor(1, 0, 0, 1);
            renderer.rectLine(playerDrawX + 4, playerDrawY + 4, rayDrawX + 4, rayDrawY + 4, 3);

            rayAngle += ONE_STEP;
            if(rayAngle < 0) {
                rayAngle += 2f*(float)Math.PI;
            }
            if(rayAngle > 2*Math.PI) {
                rayAngle -= 2f*(float)Math.PI;
            }
        }
    }

    public void draw3DScene(int ray, float rayAngle, final float finalDistance) {
        float ca = playerAngle - rayAngle;
        if(ca < 0) {
            ca += 2f*(float)Math.PI;
        }
        if(ca > 2*Math.PI) {
            ca -= 2f*(float)Math.PI;
        }

        float lineHeight = (float) (mapSize*this.windowHeight / (finalDistance * Math.cos(ca)));
        float lineOffset = this.windowHeight/2f - lineHeight/2f;

        if(lineHeight > this.windowHeight) lineHeight = this.windowHeight;
        renderer.rectLine(ray * 8 + 530, lineOffset, ray * 8 + 530, lineHeight + lineOffset, 8);
    }

    public float distance(float aX, float aY, float bX, float bY, float angle) {
        return (float)Math.sqrt((bX - aX) * (bX - aX) + (bY - aY) * (bY - aY));
    }
}
