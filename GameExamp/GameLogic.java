package tr.org.linux.kamp.GameExamp;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JLabel;

import tr.org.linux.kamp.WindowBuilder.FirstPanel.Difficulty;

public class GameLogic {

	private Player player;
	private ArrayList<GameObject> gameObjects;

	private GameFrame gameFrame;
	private GamePanel gamePanel;

	private boolean isGameRunning;
	private int xTarget;
	private int yTarget;
	private ArrayList<GameObject> chipsToRemove;
	private ArrayList<GameObject> minesToRemove;
	private ArrayList<GameObject> enemiesToRemove;

	private Random random;

	public GameLogic(String name, Color color, Difficulty dif) {
		player = new Player(10, 10, 15, color, 4, name);

		gameObjects = new ArrayList<GameObject>();
		gameObjects.add(player);
		gameFrame = new GameFrame();
		gamePanel = new GamePanel(gameObjects);
		chipsToRemove = new ArrayList<>();
		enemiesToRemove = new ArrayList<>();
		minesToRemove = new ArrayList<>();
		random = new Random();
		switch (dif) {
		case EASY:
			fillChips(40);
			fillMines(3);
			fillEnemies(2);
			break;
		case NORMAL:
			fillChips(19);
			fillMines(5);
			fillEnemies(5);
			break;
		case HARD:
			fillChips(17);
			fillMines(8);
			fillEnemies(5);
			break;
		}

		addMouseEvent();

	}

	private synchronized void checkCollisions() {

		for (GameObject gameObject : gameObjects) {
			if (player.getRectangle().intersects(gameObject.getRectangle())) {
				if (gameObject instanceof Chip) {
					player.setRadius(player.getRadius() + gameObject.getRadius());
					chipsToRemove.add(gameObject);

				}
				if (gameObject instanceof Mine) {
					player.setRadius((int) player.getRadius() / 2);
					minesToRemove.add(gameObject);

				}
				if (gameObject instanceof Enemy) {
					if (player.getRadius() > gameObject.getRadius()) {
						player.setRadius(player.getRadius() + gameObject.getRadius());
						enemiesToRemove.add(gameObject);
					} else if (player.getRadius() < gameObject.getRadius()) {
						gameObject.setRadius(player.getRadius() + gameObject.getRadius());
						/////// GAME OVER
						isGameRunning = false;
					}

				}
			}
			if (gameObject instanceof Enemy) {
				Enemy enemy = (Enemy) gameObject;

				for (GameObject gameObject2 : gameObjects) {
					if (enemy.getRectangle().intersects(gameObject2.getRectangle())) {
						if (gameObject2 instanceof Chip) {
							enemy.setRadius(enemy.getRadius() + gameObject2.getRadius());
							chipsToRemove.add(gameObject2);
						}
						if (gameObject2 instanceof Mine) {
							enemy.setRadius((int) enemy.getRadius() / 2);
							minesToRemove.add(gameObject2);
						}
					}
				}
			}
		}

		gameObjects.removeAll(chipsToRemove);
		gameObjects.removeAll(enemiesToRemove);
		gameObjects.removeAll(minesToRemove);

	}

	private synchronized void addNewObjects() {
		fillChips(chipsToRemove.size());
		fillMines(minesToRemove.size());
		fillEnemies(enemiesToRemove.size());
		enemiesToRemove.clear();
		minesToRemove.clear();
		chipsToRemove.clear();

	}

	private synchronized void movePlayer() {

		if (xTarget > player.getX()) {
			player.setX(player.getX() + player.getSpeed());
		} else if (xTarget < player.getX()) {
			player.setX(player.getX() - player.getSpeed());
		}
		if (yTarget > player.getY()) {
			player.setY(player.getY() + player.getSpeed());
		} else if (yTarget < player.getY()) {
			player.setY(player.getY() - player.getSpeed());
		}

	}

	private synchronized void moveEnemy() {
		for (GameObject enemy : gameObjects) {
			if (enemy instanceof Enemy) {
				if (enemy.getRadius() < player.getRadius()) {
					int distance = (int) Point.distance(player.getX(), player.getY(), enemy.getX(), enemy.getY());
					int newX = enemy.getX() + enemy.getSpeed();
					int newY = enemy.getY() + enemy.getSpeed();

					if (CalculateNewDistanceToEnemy((Enemy) enemy, distance, newX, newY)) {
						continue;
					}

					newX = enemy.getX() + enemy.getSpeed();
					newY = enemy.getY() - enemy.getSpeed();
					if (CalculateNewDistanceToEnemy((Enemy) enemy, distance, newX, newY)) {
						continue;
					}

					newX = enemy.getX() - enemy.getSpeed();
					newY = enemy.getY() + enemy.getSpeed();
					if (CalculateNewDistanceToEnemy((Enemy) enemy, distance, newX, newY)) {
						continue;
					}

					newX = enemy.getX() - enemy.getSpeed();
					newY = enemy.getY() - enemy.getSpeed();
					if (CalculateNewDistanceToEnemy((Enemy) enemy, distance, newX, newY)) {
						continue;
					}

				} else {

					if (player.getX() > enemy.getX()) {
						enemy.setX(enemy.getX() + enemy.getSpeed());
					} else if (player.getX() < enemy.getX()) {
						enemy.setX(enemy.getX() - enemy.getSpeed());
					}
					if (player.getY() > enemy.getY()) {
						enemy.setY(enemy.getY() + enemy.getSpeed());
					} else if (player.getY() < enemy.getY()) {
						enemy.setY(enemy.getY() - enemy.getSpeed());
					}
				}
			}
		}
	}

	private boolean CalculateNewDistanceToEnemy(Enemy enemy, int distance, int X, int Y) {
		int newDistance = (int) Point.distance(player.getX(), player.getY(), X, Y);
		if (newDistance > distance) {
			enemy.setX(X);
			enemy.setY(Y);
			return true;
		}
		return false;
	}

	private synchronized void fillChips(int n) {
		for (int i = 0; i < n; i++) {
			gameObjects.add(new Chip(random.nextInt(1000), random.nextInt(1000), 10, Color.GREEN, 0));

		}

	}

	private void fillEnemies(int n) {
		for (int i = 0; i < n; i++) {
			Enemy enemy = new Enemy(random.nextInt(1000), random.nextInt(1000), random.nextInt(10) + 10,
					Color.DARK_GRAY, 1);

			while (player.getRectangle().intersects(enemy.getRectangle())) {
				enemy.setX(random.nextInt(750));
				enemy.setY(random.nextInt(750));
			}
			gameObjects.add(enemy);
		}
	}

	private void fillMines(int n) {
		for (int i = 0; i < n; i++) {
			Mine mine = new Mine(random.nextInt(1000), random.nextInt(1000), 14, Color.RED, 0);
			while (player.getRectangle().intersects(mine.getRectangle())) {
				mine.setX(random.nextInt(750));
				mine.setY(random.nextInt(750));
			}
			gameObjects.add(mine);
		}
	}

	private void startGame() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (isGameRunning) {
					movePlayer();
					checkCollisions();
					addNewObjects();
					moveEnemy();
					gamePanel.repaint();
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	public void startApp() {
		gameFrame.setContentPane(gamePanel);
		gameFrame.setVisible(true);
		isGameRunning = true;
		startGame();

	}

	private void addMouseEvent() {
		gameFrame.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {

			}
		});

		gamePanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				xTarget = e.getX();
				yTarget = e.getY();

			}

			@Override
			public void mouseDragged(MouseEvent e) {

			}
		});

	}
}
