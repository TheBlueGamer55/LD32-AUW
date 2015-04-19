package com.swinestudios.unusualweapon;

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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Gameplay implements GameScreen{

	public static int ID = 2;

	public float camX;
	public float camY;

	public ArrayList<Rectangle> solids;
	public ArrayList<Bubble> bubbles;
	public ArrayList<Enemy> enemies;

	public Player player;
	public boolean foundEmptySpace;

	public Enemy testEnemy;

	public CaveSystem cave;
	public Sprite caveTile;

	@Override
	public int getId(){
		return ID;
	}

	@Override
	public void initialise(GameContainer gc){
		caveTile = new Sprite(new Texture(Gdx.files.internal("testCaveTile16.png")));
		caveTile.setOrigin(0, 0);
		caveTile.flip(false, true);
	}

	@Override
	public void interpolate(GameContainer gc, float delta){
	}

	@Override
	public void postTransitionIn(Transition t){

	}

	@Override
	public void postTransitionOut(Transition t){

	}

	@Override
	public void preTransitionIn(Transition t){
		foundEmptySpace = false;
		
		solids = new ArrayList<Rectangle>();
		bubbles = new ArrayList<Bubble>();
		enemies = new ArrayList<Enemy>();

		player = new Player(320, 240, this);
		testEnemy = new Enemy( 320, 240, 16, 16, this);
		enemies.add(testEnemy);

		camX = player.x - Gdx.graphics.getWidth() / 2;
		camY = player.y - Gdx.graphics.getHeight() / 2;

		cave = new CaveSystem(-120, -120, this);
		cave.generateTerrain();
		cave.addOptimizedTerrain();

		for(int i = 1; i < cave.terrain.length - 1; i++){
			for(int j = cave.terrain[i].length - 2; j > 0; j--){
				//if the 3x3 space is empty, move the player here
				if( cave.terrain[i][j] + 
					cave.terrain[i][j+1] + 
					cave.terrain[i][j-1] + 
					cave.terrain[i+1][j] + 
					cave.terrain[i-1][j] +
					cave.terrain[i+1][j+1] +
					cave.terrain[i+1][j-1] +
					cave.terrain[i-1][j+1] +
					cave.terrain[i-1][j-1] == 0){
					player.x = cave.x + j * cave.tileSize + 4;
					player.y = cave.y + i * cave.tileSize + 4;
					foundEmptySpace = true;;
				}
			}
			if(foundEmptySpace){
				break;
			}
		}

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

		for(int i = 0; i<bubbles.size(); i++){
			bubbles.get(i).render( g );
		}

		for(int i = 0; i<enemies.size(); i++){
			enemies.get(i).render( g );
		}
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> sm, float delta){
		camX = player.x - Gdx.graphics.getWidth() / 2;
		camY = player.y - Gdx.graphics.getHeight() / 2;
		player.update(delta);	

		for(int i = 0; i<bubbles.size(); i++){
			bubbles.get(i).update(delta);
			if(bubbles.get(i).delete == true){
				bubbles.remove(i);
			}
		}

		for(int i = 0; i<enemies.size(); i++){
			enemies.get(i).update(delta);
			if(enemies.get(i).delete == true){
				enemies.remove(i);
			}
		}

		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
			sm.enterGameScreen(MainMenu.ID, new FadeOutTransition(), new FadeInTransition());
		}
	}

	//Temporary helper method for testing
	public void renderSolids(Graphics g){
		for(int i = 0; i < solids.size(); i++){
			solids.get(i).draw(g);
		}
	}

	/*
	 * Draws cave tiles based on a generated cave system
	 */
	public void renderCave(Graphics g){
		for(int i = 0; i < cave.terrain.length; i++){
			for(int j = 0; j < cave.terrain[i].length; j++){
				if(cave.terrain[i][j] == 1){
					g.drawSprite(caveTile, cave.x + j * cave.tileSize, cave.y + i * cave.tileSize);
				}
			}
		}
	}

}
