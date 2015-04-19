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

public class Gameplay implements GameScreen{

	public static int ID = 2;

	public float camX;
	public float camY;

	public ArrayList<Rectangle> solids;
	public ArrayList<Bubble> bubbles;

	public Player player;
	
	public CaveSystem cave;

	@Override
	public int getId(){
		return ID;
	}

	@Override
	public void initialise(GameContainer gc){

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
		solids = new ArrayList<Rectangle>();
		bubbles = new ArrayList<Bubble>();

		player = new Player(320, 240, this);
		camX = player.x - Gdx.graphics.getWidth() / 2;
		camY = player.y - Gdx.graphics.getHeight() / 2;
		
		cave = new CaveSystem(-120, -120, this);
		cave.generateTerrain();
		cave.addTerrain();

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
		g.drawString("This is the gameplay screen", 320, 240);
		renderSolids(g);
		player.render(g);

		for(int i = 0; i<bubbles.size(); i++){
			bubbles.get(i).render( g );
		}
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> sm, float delta){
		camX = player.x - Gdx.graphics.getWidth() / 2;
		camY = player.y - Gdx.graphics.getHeight() / 2;
		player.update(delta);	

		for(int i = 0; i<bubbles.size(); i++){
			bubbles.get(i).update(delta);
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

}
