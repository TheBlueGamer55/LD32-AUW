package com.swinestudios.unusualweapon;

import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.core.screen.Transition;
import org.mini2Dx.core.screen.transition.FadeInTransition;
import org.mini2Dx.core.screen.transition.FadeOutTransition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class MainMenu implements GameScreen{
	
	public static int ID = 1;
	
	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>";
	
	public BitmapFont font;
	
	public Sprite textBackground;
	
	public CaveSystem border;
	public int[][] menuTerrain = new int[Gdx.graphics.getHeight() / CaveSystem.tileSize][Gdx.graphics.getWidth() / CaveSystem.tileSize];
	
	@Override
	public int getId(){
		return ID;
	}

	@Override
	public void initialise(GameContainer gc){
		textBackground = new Sprite(new Texture(Gdx.files.internal("mainMenuTextBG.png")));
		Global_Constants.adjustSprite(textBackground);
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel-UniCode.ttf"));
		font = generator.generateFont(24, MainMenu.FONT_CHARACTERS, true);
		generator.dispose();
		
		border = new CaveSystem(0, 0, null);
		border.terrain = menuTerrain;
		for(int i = 0; i < border.terrain.length; i++){
			for(int j = 0; j < border.terrain[i].length; j++){
				if(i == 0 || j == 0 || i == border.terrain.length - 1 || j == border.terrain[i].length - 1 || i == border.terrain.length - 4){
					border.terrain[i][j] = 1;
				}
			}
		}
		border.setTileTypes();
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
		
	}

	@Override
	public void preTransitionOut(Transition t){
		
	}

	@Override
	public void render(GameContainer gc, Graphics g){
		g.setFont(font);
		renderCave(g);
		g.setBackgroundColor(new Color(26 / 255f, 168 / 255f, 196 / 255f, 0));
		g.drawSprite(textBackground, 0, 0);
		g.drawString("Max levels completed in one run: " + Gameplay.maxLevelCount, 198, 402);
		g.drawString("Current run: " + Gameplay.levelCount, 198, 416);
	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> sm, float delta) {
		if(Gdx.input.isKeyJustPressed(Keys.ENTER)){
			sm.enterGameScreen(Gameplay.ID, new FadeOutTransition(), new FadeInTransition());
		}
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
			Gdx.app.exit();
		}
	}
	
	/*
	 * Draws cave tiles based on a generated cave system
	 */
	public void renderCave(Graphics g){
		for(int i = 0; i < border.terrain.length; i++){
			for(int j = 0; j < border.terrain[i].length; j++){
				if(border.terrain[i][j] == 1){
					int type = border.tileTypes[i][j];
					int row = type / 8; //the width of the tileset in tiles is 8
					int col = type % 8;
					g.drawSprite(Gameplay.caveTiles[row][col], border.x + j * CaveSystem.tileSize, border.y + i * CaveSystem.tileSize);
				}
			}
		}
	}

}
