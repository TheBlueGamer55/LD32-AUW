package com.swinestudios.unusualweapon;

import java.awt.Point;
import java.util.ArrayList;

import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Gameplay implements GameScreen{

	public static int ID = 2;
	
	public static Sound theme;
	
	public static int maxLevelCount = 0;
	public static int levelCount = 0;
	public static float CONSTANT_HEALTH = 100.0f;
	public int originalTreasureCount;
	public final int winPercent = 77; //TODO adjust later
	
	public int treasureCount = 0;

	public float camX;
	public float camY;

	public ArrayList<Rectangle> solids;
	public ArrayList<Bubble> bubbles;
	public ArrayList<Enemy> enemies;
	public ArrayList<Treasure> treasures;

	public Player player;
	public boolean foundEmptySpace;

	public boolean gameOver = false;
	public boolean paused = false;

	public CaveSystem cave;
	public Sprite caveTileset;
	public TextureRegion[][] tiles;
	public static Sprite[][] caveTiles;

	public Sound pop;

	public final int treasureChance = 20; //Percent chance of treasure spawning at each "platform"
	public final int enemyChance = 15; //Percent chance of enemies spawning at each "ceiling"

	@Override
	public int getId(){
		return ID;
	}

	@Override
	public void initialise(GameContainer gc){
		theme = Gdx.audio.newSound(Gdx.files.internal("endawawa.ogg"));
		caveTileset = new Sprite(new Texture(Gdx.files.internal("underwaterCaveTileset.png")));
		caveTileset.setOrigin(0, 0);
		tiles = caveTileset.split(16, 16);
		caveTiles = new Sprite[tiles.length][tiles[0].length];
		caveTileset.flip(false, true);
		for(int i = 0; i < tiles.length; i++){
			for(int j = 0; j < tiles[i].length; j++){
				tiles[i][j].flip(false, true);
				caveTiles[i][j] = new Sprite(new TextureRegion(tiles[i][j]));
				caveTiles[i][j].setSize(CaveSystem.tileSize, CaveSystem.tileSize);
			}
		}
		pop = Gdx.audio.newSound(Gdx.files.internal("pop2.wav"));
		//caveTileset.setSize(caveTileset.getWidth() * (CaveSystem.tileSize / 16), caveTileset.getHeight() * (CaveSystem.tileSize / 16));
	}

	@Override
	public void interpolate(GameContainer gc, float delta){
	}

	@Override
	public void postTransitionIn(Transition t){
		theme.loop();
	}

	@Override
	public void postTransitionOut(Transition t){
		gameOver = false;
		paused = false;
		theme.stop();
	}

	@Override
	public void preTransitionIn(Transition t){
		gameOver = false;
		paused = false;
		foundEmptySpace = false;

		solids = new ArrayList<Rectangle>();
		bubbles = new ArrayList<Bubble>();
		enemies = new ArrayList<Enemy>();
		treasures = new ArrayList<Treasure>();

		player = new Player(-150, -150, this);

		camX = player.x - Gdx.graphics.getWidth() / 2;
		camY = player.y - Gdx.graphics.getHeight() / 2;

		cave = new CaveSystem(-100, -100, this);
		cave.generateTerrain();
		cave.addOptimizedTerrain();

		ArrayList<Point> emptySpaces = cave.findEmptySpace();
		int spawnIndex = Global_Constants.random.nextInt(emptySpaces.size());
		player.x = (float)(cave.x + emptySpaces.get(spawnIndex).getX() * CaveSystem.tileSize);
		player.y = (float)(cave.y + emptySpaces.get(spawnIndex).getY() * CaveSystem.tileSize);

		spawnTreasure();
		spawnEnemies();
		originalTreasureCount = treasures.size();
		
		player.health = CONSTANT_HEALTH;

		//Input handling
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(player);
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void preTransitionOut(Transition t){
		
	}

	@Override
	public void render(GameContainer gc, Graphics g){
		g.translate((float) Math.round(camX), (float) Math.round(camY)); //camera movement
		renderCave(g);
		//renderSolids(g);
		player.render(g);
		renderTreasures(g);

		for(int i = 0; i<bubbles.size(); i++){
			bubbles.get(i).render( g );
		}

		for(int i = 0; i<enemies.size(); i++){
			enemies.get(i).render( g );
		}

		if(gameOver){
			g.setColor(Color.WHITE);
			g.drawString("You died! Press Escape to go back to the main menu", camX + 160, camY + 240);
		}
		if(paused){
			g.setColor(Color.WHITE);
			g.drawString("Are you sure you want to quit? Y or N", camX + 220, camY + 240);
		}
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> sm, float delta){
		if(!gameOver && !paused){
			camX = player.x - Gdx.graphics.getWidth() / 2;
			camY = player.y - Gdx.graphics.getHeight() / 2;
			player.update(delta);	

			for(int i = 0; i<bubbles.size(); i++){
				bubbles.get(i).update(delta);
				if(bubbles.get(i).delete == true){
					pop.play();

					bubbles.remove(i);
				}
			}

			for(int i = 0; i<enemies.size(); i++){
				if(enemies.get(i).distanceTo(player.hitbox) <= enemies.get(i).LEASH_DISTANCE * 4){ //Needed to prevent lag
					enemies.get(i).update(delta);
				}
				if(enemies.get(i).delete == true){
					enemies.remove(i);
				}
			}

			updateTreasures(delta);

			if(player.health <= 0){
				gameOver = true;
			}
			
			System.out.println((float)treasures.size() / originalTreasureCount * 100f);
			if((float)treasures.size() / originalTreasureCount * 100f <= winPercent){
				Gameplay.levelCount++;
				if(levelCount > maxLevelCount){
					maxLevelCount = levelCount;
				}
				this.preTransitionIn(new FadeOutTransition());
			}
			
			if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
				paused = true;
			}
		}
		else{
			if(gameOver){
				if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
					Gameplay.levelCount = 0;
					CONSTANT_HEALTH = 100f;
					sm.enterGameScreen(MainMenu.ID, new FadeOutTransition(), new FadeInTransition());
				}
			}
			else if(paused){
				if(Gdx.input.isKeyJustPressed(Keys.Y)){
					sm.enterGameScreen(MainMenu.ID, new FadeOutTransition(), new FadeInTransition());
				}
				if(Gdx.input.isKeyJustPressed(Keys.N)){
					paused = false;
				}
			}
		}
	}

	//Temporary helper method for testing
	public void renderSolids(Graphics g){
		for(int i = 0; i < solids.size(); i++){
			solids.get(i).draw(g);
		}
	}

	public void renderTreasures(Graphics g){
		for(int i = 0; i < treasures.size(); i++){
			treasures.get(i).render(g);
		}
	}

	public void updateTreasures(float delta){
		for(int i = 0; i < treasures.size(); i++){
			treasures.get(i).update(delta);
		}
	}

	/*
	 * Draws cave tiles based on a generated cave system
	 */
	public void renderCave(Graphics g){
		for(int i = 0; i < cave.terrain.length; i++){
			for(int j = 0; j < cave.terrain[i].length; j++){
				if(cave.terrain[i][j] == 1){
					int type = cave.tileTypes[i][j];
					int row = type / 8; //the width of the tileset in tiles is 8
					int col = type % 8;
					g.drawSprite(caveTiles[row][col], cave.x + j * CaveSystem.tileSize, cave.y + i * CaveSystem.tileSize);
				}
			}
		}
	}

	/*
	 * Spawns treasure in the cave.
	 */
	public void spawnTreasure(){
		for(int i = 2; i < cave.terrain.length; i++){
			for(int j = 0; j < cave.terrain[i].length; j++){
				if(cave.terrain[i][j] == 1 && cave.terrain[i-1][j] == 0){ //a "platform"
					if(Global_Constants.random.nextInt(100) <= treasureChance){
						treasures.add(new Treasure(cave.x + j * CaveSystem.tileSize + 3, cave.y + i * CaveSystem.tileSize, this));
					}
				}
			}
		}
	}

	/*
	 * Spawns enemies in the cave.
	 */
	public void spawnEnemies(){
		for(int i = 2; i < cave.terrain.length - 2; i++){
			for(int j = 0; j < cave.terrain[i].length; j++){
				if( cave.terrain[i][j] == 1 && 
						cave.terrain[i+1][j] == 0 &&
						cave.terrain[i+1][j-1] == 0 &&
						cave.terrain[i+1][j+1] == 0){ //a 3-block-wide "ceiling"
					if(Global_Constants.random.nextInt(100) <= enemyChance){
						//treasures.add(new Treasure(cave.x + j * CaveSystem.tileSize + 3, cave.y + i * CaveSystem.tileSize, this));
						enemies.add(new Enemy(cave.x + j * CaveSystem.tileSize + 3, cave.y + 4 + (i+1) * CaveSystem.tileSize, 16, 16, this));
					}
				}
			}
		}
	}

}
