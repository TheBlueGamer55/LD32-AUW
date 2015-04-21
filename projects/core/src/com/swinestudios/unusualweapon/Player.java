package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Animation;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player implements InputProcessor{ 

	public float x, y;
	public float velX, velY;
	public float accelX, accelY;

	public final float frictionX = 0.6f;
	public final float frictionY = 0.6f;

	public final float moveSpeedX = 2.0f;
	public final float moveSpeedY = 2.0f;

	public final float maxSpeedX = 2.0f;
	public final float maxSpeedY = 2.0f;

	public boolean isActive;

	public boolean facingRight, facingLeft;

	public final int totalBubbleAmmo = 8;
	public int bubbleAmmo = 8;
	public final float totalReloadTime = 10;
	public float elapsedReloadTime = 0;


	//HEALTH AND HEALTHBAR STUFF
	public float maxHealth = 100.0f;
	public float health = maxHealth;

	public float healthBarMaxWidth = 200;
	public float healthBarHeight = 16;
	
	public final float damage = 15f; //TODO adjust later
	public float damageTimer = 0f;
	public float maxDamageTimer = 1f;
	public boolean isDamaged = false;

	//Obsolete - public float healthBarX = 400;
	public float healthBarY = 16;

	public float healthBarRed = 0;
	public float healthBarGreen = 0;
	public float healthBarBlue = 0;
	//-----------------------------------

	public Rectangle hitbox;
	public Gameplay level;
	public String type;
	
	public Sound bubbleFire;
	public Sound pickupTreasure;
	public Sound hit;
	
	public Sprite left, left1, left2, right, right1, right2;
	public Animation<Sprite> playerLeft, playerRight, playerCurrent;
	public final float animationSpeed = 0.2f; //the # of seconds each frame lasts

	//Controls/key bindings
	public final int LEFT = Keys.LEFT;
	public final int RIGHT = Keys.RIGHT;
	public final int UP = Keys.UP;
	public final int DOWN = Keys.DOWN;

	public Player(float x, float y, Gameplay level){
		this.x = x;
		this.y = y;
		velX = 0;
		velY = 0;
		accelX = 0;
		accelY = 0;
		isActive = false;
		this.level = level;
		type = "Player";
		bubbleFire = Gdx.audio.newSound(Gdx.files.internal("bubbleHit.wav"));
		pickupTreasure = Gdx.audio.newSound(Gdx.files.internal("pickupTreasure.wav"));
		hit = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
		
		//Initialize art
		facingRight = true;
		facingLeft = false;
		
		right = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_right.png")));
		right1 = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_right_1.png")));
		right2 = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_right_2.png")));
		
		left = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_left.png")));
		left1 = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_left_1.png")));
		left2 = new Sprite(new Texture(Gdx.files.internal("playerFrames/diver_left_2.png")));
		
		adjustSprite(right, right1, right2, left, left1, left2);
		
		playerLeft = new Animation<Sprite>(); //left animation
		playerRight = new Animation<Sprite>(); //right animation
		
		playerLeft.addFrame(left, 0);
		playerLeft.addFrame(left1, animationSpeed);
		playerLeft.addFrame(left2, animationSpeed);
		playerLeft.setLooping(false);
		playerLeft.flip(false, true);
		
		playerRight.addFrame(right, 0);
		playerRight.addFrame(right1, animationSpeed);
		playerRight.addFrame(right2, animationSpeed);
		playerRight.setLooping(false);
		playerRight.flip(false, true);
		
		playerCurrent = playerRight;
		
		hitbox = new Rectangle(x, y, right.getWidth(), right.getHeight()); 
	}

	public void render(Graphics g){
		g.setColor(Color.GREEN);
		if(bubbleAmmo == 0){
			g.setColor(Color.RED);
		}
		
		if(velX != 0 || velY != 0){ //if moving, draw animated sprites
			playerCurrent.draw(g, x, y);
		}
		else{ //draw still images if not moving, with appropriate direction
			if(facingRight){
				g.drawSprite(right, x, y);
			}
			else{
				g.drawSprite(left, x, y);
			}
		}

		//these two lines are the ammo/reload indicators.
		g.drawRect(16 + level.camX, 16 + level.camY, 64, 16);
		g.fillRect(16 + level.camX, 16 + level.camY, 64 * (float) (bubbleAmmo != totalBubbleAmmo ? (float)elapsedReloadTime / totalReloadTime : 1), 16);

		g.setColor(Color.WHITE);
		g.drawRect(Gdx.graphics.getWidth() - (healthBarMaxWidth + 16) + level.camX, healthBarY + level.camY, healthBarMaxWidth, healthBarHeight);

		g.setColor(new Color(healthBarRed, healthBarGreen, healthBarBlue, 1.0f));
		g.fillRect(Gdx.graphics.getWidth() - (healthBarMaxWidth + 16) + level.camX, healthBarY + level.camY, Global_Constants.minZero(healthBarMaxWidth * getHealthPercentage()), healthBarHeight);
	}

	public void update(float delta){
		accelX = 0;
		accelY = 0;

		playerMovement();

		//Apply friction when not moving or when exceeding the max horizontal speed
		if(Math.abs(velX) > maxSpeedX || !Gdx.input.isKeyPressed(this.LEFT) && !Gdx.input.isKeyPressed(this.RIGHT)){
			friction(true, false);
		}
		//Apply friction when not moving or when exceeding the max vertical speed
		if(Math.abs(velY) > maxSpeedY || !Gdx.input.isKeyPressed(this.UP) && !Gdx.input.isKeyPressed(this.DOWN)){
			friction(false, true);
		}

		limitSpeed(true, true);
		move();
		hitbox.setX(this.x);
		hitbox.setY(this.y);

		updateSprite(delta);
		
		shooting(delta);

		healthBarColoring();

		checkEnemyCollision();
		if(isDamaged){
			damageTimer += delta;
			if(damageTimer >= maxDamageTimer){
				damageTimer = 0;
				isDamaged = false;
			}
		}
	}
	
	public void updateSprite(float delta){
		//change the direction the player is facing
		if(facingRight){
			playerCurrent = playerRight;
		}
		else{
			playerCurrent = playerLeft;
		}
		//check if the player is moving or not to set up static or animated frames
		if(velX != 0 || velY != 0){
			playerLeft.setLooping(true);
			playerRight.setLooping(true);
		}
		else{
			playerLeft.setLooping(false);
			playerRight.setLooping(false);
		}
		playerCurrent.update(delta);
	}

	public void healthBarColoring(){
		if( getHealthPercentage() == 1.0f ){
			healthBarRed = 1.0f;
			healthBarGreen = 1.0f;
			healthBarBlue = 1.0f;
		}
		else{
			healthBarRed = Global_Constants.minZero( (float) (1.0 - getHealthPercentage()) );
			healthBarGreen = Global_Constants.minZero( (float) (getHealthPercentage()) );
			healthBarBlue = 0;
		}
	}


	public void shooting(float delta){//Handles shooting and reloading.
		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && bubbleAmmo > 0){//On left-click, fire a bubble. The rate will need to be limited in the future.
			if(bubbleAmmo == totalBubbleAmmo){
				bubbleFire.play();
			}
			level.bubbles.add(new Bubble(this.x+16, this.y+16, Gdx.input.getX() + level.camX, Gdx.input.getY() + level.camY, this.level));
			bubbleAmmo--;
		}

		else if(bubbleAmmo == 0){//making this "else if" saves on performance a tiny bit
			elapsedReloadTime += delta;
			if(elapsedReloadTime >= totalReloadTime){
				bubbleAmmo = totalBubbleAmmo;
				elapsedReloadTime = 0;
			}
		}
	}

	public void playerMovement(){
		//Move Left
		if(Gdx.input.isKeyPressed(this.LEFT) && velX > -maxSpeedX){
			accelX = -moveSpeedX;
			facingRight = false;
			facingLeft = true;
		}
		//Move Right
		if(Gdx.input.isKeyPressed(this.RIGHT) && velX < maxSpeedX){
			accelX = moveSpeedX;
			facingRight = true;
			facingLeft = false;
		}
		//Move Up
		if(Gdx.input.isKeyPressed(this.UP) && velY > -maxSpeedY){
			accelY = -moveSpeedY;
		}
		//Move Down
		if(Gdx.input.isKeyPressed(this.DOWN) && velY < maxSpeedY){
			accelY = moveSpeedY;
		}
	}

	public void checkEnemyCollision(){
		for(int i = 0; i<level.enemies.size(); i++){
			if(isColliding(level.enemies.get(i).hitbox, x, y)){
				if(!isDamaged){
					damagePlayer(damage);
					isDamaged = true;
				}
			}
		}
	}

	/*
	 * Checks if there is a collision if the player was at the given position.
	 */
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

	public void move(){
		moveX();
		moveY();
	}

	/*
	 * Applies a friction force in the given axes by subtracting the respective velocity components
	 * with the given friction components.
	 */
	public void friction(boolean horizontal, boolean vertical){
		//if there is horizontal friction
		if(horizontal){
			if(velX > 0){
				velX -= frictionX; //slow down
				if(velX < 0){
					velX = 0;
				}
			}
			if(velX < 0){
				velX += frictionX; //slow down
				if(velX > 0){
					velX = 0;
				}
			}
		}
		//if there is vertical friction
		if(vertical){
			if(velY > 0){
				velY -= frictionY; //slow down
				if(velY < 0){
					velY = 0;
				}
			}
			if(velY < 0){
				velY += frictionY; //slow down
				if(velY > 0){
					velY = 0;
				}
			}
		}
	}

	/*
	 * Limits the speed of the player to a set maximum
	 */
	protected void limitSpeed(boolean horizontal, boolean vertical){
		//If horizontal speed should be limited
		if(horizontal){
			if(Math.abs(velX) > maxSpeedX){
				velX = maxSpeedX * Math.signum(velX);
			}
		}
		//If vertical speed should be limited
		if(vertical){
			if(Math.abs(velY) > maxSpeedY){
				velY = maxSpeedY * Math.signum(velY);
			}
		}
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
	 * Move horizontally in the direction of the x-velocity vector. If there is a collision in
	 * this direction, step pixel by pixel up until the player hits the solid.
	 */
	public void moveX(){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x + velX, y)){
				while(!isColliding(solid, x + Math.signum(velX), y)){
					x += Math.signum(velX);
				}
				velX = 0;
			}
		}
		x += velX;
		velX += accelX;
	}

	/*
	 * Move vertically in the direction of the y-velocity vector. If there is a collision in
	 * this direction, step pixel by pixel up until the player hits the solid.
	 */
	public void moveY(){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x, y + velY)){
				while(!isColliding(solid, x, y + Math.signum(velY))){
					y += Math.signum(velY);
				}
				velY = 0;
			}
		}
		y += velY;
		velY += accelY;
	}

	public float getHealthPercentage(){
		return(health/maxHealth);
	}
	
	public void damagePlayer(float amount){
		this.health -= amount;
		Gameplay.CONSTANT_HEALTH -= amount;
		hit.play();
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
	
	/*
	 * Used on animations to flip the sprites correctly
	 */
	public void flipFrames(Animation<Sprite> a){
		for(int i = 0; i < a.getNumberOfFrames(); i++){
			a.getFrame(i).flip(false, true);
		}
	}

	public void setFrameOrigins(Animation<Sprite> a){
		for(int i = 0; i < a.getNumberOfFrames(); i++){
			a.getFrame(i).setOrigin(0, 0);
		}
	}

	//========================================Input Methods==============================================

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}

