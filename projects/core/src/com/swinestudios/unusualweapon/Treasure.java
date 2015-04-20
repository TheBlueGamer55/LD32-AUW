package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Treasure {

	public float x, y;

	public boolean isActive;

	public Rectangle hitbox;
	public Gameplay level;
	public String type;
	public Sprite treasureSprite;

	public Treasure(float x, float y, Gameplay level){
		this.x = x;
		this.y = y;
		isActive = false;
		this.level = level;
		type = "Treasure";
		treasureSprite = new Sprite(new Texture(Gdx.files.internal("treasure.png")));
		adjustSprite(treasureSprite);
		hitbox = new Rectangle(x, y, treasureSprite.getWidth(), treasureSprite.getHeight());
		hitbox.setY(y - treasureSprite.getHeight());
	}

	public void render(Graphics g){
		g.drawSprite(treasureSprite, x, y - treasureSprite.getHeight());
	}


	public void update(float delta){		
		checkPlayerCollision();
	}

	public void checkPlayerCollision(){
		if(hitbox.intersects(level.player.hitbox)){
			level.player.pickupTreasure.play();
			level.treasures.remove(this); 
		}
	}

	public boolean isColliding(Rectangle other, float x, float y){
		if(other == this.hitbox){ //Make sure solid isn't stuck on itself
			return false;
		}
		if(x < other.x + other.width && x + hitbox.width > other.x && y < other.y + other.height && y + hitbox.height > other.y){
			return true;
		}
		return false;
	}

	/*
	 * Helper method for checking whether there is a collision if the player moves at the given position
	 */
	public boolean collisionExistsAt(float x, float y){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x, y)){
				return true;
			}
		}
		return false;
	}

	/*
	 * Returns the current tile position of the player, given the specific tile dimensions
	 */
	public float getTileX(int tileSize){
		return (int)(x / tileSize) * tileSize;
	}

	/*
	 * Returns the current tile position of the player, given the specific tile dimensions
	 */
	public float getTileY(int tileSize){
		return (int)(y / tileSize) * tileSize;
	}

	/*
	 * Returns the distance between the player and the given target
	 */
	public float distanceTo(Rectangle target){
		return ((float)Math.pow(Math.pow((target.y - this.y), 2.0) + Math.pow((target.x - this.x), 2.0), 0.5));
	}

	/*
	 * Sets up any images that the player may have. Necessary because images are flipped and have the origin
	 * on the bottom-left by default.
	 */
	public void adjustSprite(Sprite... s){
		for(int i = 0; i < s.length; i++){
			s[i].setOrigin(0, 0);
			s[i].flip(false, true);
		}
	}
}
