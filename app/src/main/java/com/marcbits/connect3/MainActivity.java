package com.marcbits.connect3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String STATE_MENU_PLAYER_MODE = "state_menu_player_mode";
    private static final String STATE_GAME_OVER = "state_game_over";
    private static final String STATE_PLAYER_MODE = "state_player_mode";
    private static final String STATE_ACTIVE_PLAYER = "state_active_player";
    private static final String STATE_GAME_STATE = "state_game_state";

    private Menu menu;
    private String menuPlayModeText;

    ImageView topLeft;
    ImageView topCenter;
    ImageView topRight;
    ImageView middleLeft;
    ImageView middleCenter;
    ImageView middleRight;
    ImageView bottomLeft;
    ImageView bottomCenter;
    ImageView bottomRight;

    private boolean gameOver = false;

    // 0 = vs Human, 1 = vs Machine
    private int playerMode = 1;

    // 0 = yellow, 1 = red
    private int activePlayer = 0;

    // 2 means unplayed
    private int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};

    private int[][] winningPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Started");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        topLeft = (ImageView) findViewById(R.id.topLeft);
        topLeft.setOnClickListener(new CustomClickListener());

        topCenter = (ImageView) findViewById(R.id.topCenter);
        topCenter.setOnClickListener(new CustomClickListener());

        topRight = (ImageView) findViewById(R.id.topRight);
        topRight.setOnClickListener(new CustomClickListener());

        middleLeft = (ImageView) findViewById(R.id.middleLeft);
        middleLeft.setOnClickListener(new CustomClickListener());

        middleCenter = (ImageView) findViewById(R.id.middleCenter);
        middleCenter.setOnClickListener(new CustomClickListener());

        middleRight = (ImageView) findViewById(R.id.middleRight);
        middleRight.setOnClickListener(new CustomClickListener());

        bottomLeft = (ImageView) findViewById(R.id.bottomLeft);
        bottomLeft.setOnClickListener(new CustomClickListener());

        bottomCenter = (ImageView) findViewById(R.id.bottomCenter);
        bottomCenter.setOnClickListener(new CustomClickListener());

        bottomRight = (ImageView) findViewById(R.id.bottomRight);
        bottomRight.setOnClickListener(new CustomClickListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(MainActivity.STATE_MENU_PLAYER_MODE, menuPlayModeText);
        outState.putBoolean(MainActivity.STATE_GAME_OVER, gameOver);
        outState.putInt(MainActivity.STATE_PLAYER_MODE, playerMode);
        outState.putInt(MainActivity.STATE_ACTIVE_PLAYER, activePlayer);
        outState.putSerializable(MainActivity.STATE_GAME_STATE, gameState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        gameOver = savedInstanceState.getBoolean(MainActivity.STATE_GAME_OVER);
        playerMode = savedInstanceState.getInt(MainActivity.STATE_PLAYER_MODE);
        activePlayer = savedInstanceState.getInt(MainActivity.STATE_ACTIVE_PLAYER);
        gameState = (int[]) savedInstanceState.getSerializable(MainActivity.STATE_GAME_STATE);

        // now set the board back based on the gameState array
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] != 2) {
                ImageView view = (ImageView) findViewById(
                        R.id.activity_main).findViewWithTag(Integer.toString(i));
                if (gameState[i] == 0) {
                    view.setImageResource(R.drawable.yellow);
                } else {
                    view.setImageResource(R.drawable.red);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reset_game) {
            playAgain();

            return true;
        }

        if (id == R.id.player_mode) {

            MenuItem modeMenuItem = menu.findItem(R.id.player_mode);

            if (playerMode == 0) {
                // we are in vs Human mode
                playerMode = 1;
                menuPlayModeText = "vs Human";
                modeMenuItem.setTitle(menuPlayModeText);
                Toast.makeText(this, "You are now in Machine mode.", Toast.LENGTH_SHORT).show();

            } else {
                // we are in vs Machine mode
                playerMode = 0;
                menuPlayModeText = "vs Machine";
                modeMenuItem.setTitle(menuPlayModeText);
                Toast.makeText(this, "You are now in Human mode.", Toast.LENGTH_SHORT).show();
            }

            playAgain();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void playAgain() {
        activePlayer = 0;

        for (int i = 0; i < gameState.length; i++) {
            // This is an unplayed cell
            ImageView img = (ImageView) findViewById(
                    R.id.activity_main).findViewWithTag(Integer.toString(i));

            img.setImageDrawable(null);
        }

        for (int i = 0; i < gameState.length; i++) {
            gameState[i] = 2;
        }

        gameOver = false;
    }

    private boolean checkIfUnplayedCells() {
        // If no unplayed cells found the game has ended.
        boolean found = false;
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] == 2) {
                found = true;
                break;
            }
        }

        return found;
    }

    private ImageView generateMove() {
        ImageView generatedMoveView = null;

        // look for unplayed cells
        // gameState -> array of 9 positions, you want the indexes where value == 2
        int availablePositions = 0;
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] == 2) {
                availablePositions++;
            }
        }
        int[][] indexes = new int[availablePositions][1];

        int counter = 0;
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] == 2) {
                indexes[counter][0] = i;
                counter++;
            }
        }

        int min = 0;
        int max = indexes.length;
        int newNumber = -1;

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < indexes.length; i++) {
            sb.append("{");
            sb.append(i + ",");
            sb.append(indexes[i][0]);
            sb.append("}");

            if (i < indexes.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");

        int nextMoveIndex = -1;
        int bestMoveIndex = findBestMove();

        if (bestMoveIndex < 0) {
            // didn't find best move

            if (indexes.length == 1) {
                newNumber = indexes[0][0];
            } else {
                // generate a random number between 0 and indexes.length
                boolean randomIsOk = false;
                Random rn = new Random();
                do {
                    newNumber = rn.nextInt(max);

                    Log.d(TAG, "generateMove: gameState: " + Arrays.toString(gameState));
                    Log.d(TAG, "generateMove: indexes: " + sb.toString());

                    nextMoveIndex = indexes[newNumber][0];

                    if (gameState[nextMoveIndex] == 2) {
                        randomIsOk = true;
                    }
                } while (!randomIsOk);
            }
        } else {
            nextMoveIndex = bestMoveIndex;
        }

        // get the cell that corresponds to this number
        Log.d(TAG, "generateMove: index/numToReplace: " + nextMoveIndex + "/" + gameState[nextMoveIndex]);

        generatedMoveView = (ImageView) findViewById(
                R.id.activity_main).findViewWithTag(Integer.toString(nextMoveIndex));

        return generatedMoveView;
    }

    private int findBestMove() {
        int bestMoveIndex = -1;

        // winningPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        //                     {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        //                     {0, 4, 8}, {2, 4, 6}};

        // 1 - Check if there is an immediate winning move
        // Remember: 1 is machine
        int machinePlayer = 1;
        mainOffensiveLoop:
        for (int[] winningPosition : winningPositions) {
            int occupiedCells = 0;
            int[] boardPosition = new int[3];
            for (int i = 0; i < 3; i++) {
                boardPosition[i] = gameState[winningPosition[i]];
                if (boardPosition[i] == machinePlayer) {
                    occupiedCells++;
                }
            }

            if (occupiedCells == 2) {
                // Two occupied cells out of a maximum three means a pending winning move!
                for (int i = 0; i < 3; i++) {
                    if (gameState[winningPosition[i]] == 2) {
                        // This is the unoccupied cell that is needed to win!
                        bestMoveIndex = winningPosition[i];

                        break mainOffensiveLoop;
                    }
                }
            }
        }

        Log.d(TAG, "findBestMove: offense: " + bestMoveIndex);

        if (bestMoveIndex < 0) {
            // 2 - If no winning move found, play defensively and check if (0 - human) opponent
            //     has an immediate winning move.
            int humanPlayer = 0;
            mainDefensiveLoop:
            for (int[] winningPosition : winningPositions) {
                int occupiedCells = 0;
                int[] boardPosition = new int[3];
                for (int i = 0; i < 3; i++) {
                    boardPosition[i] = gameState[winningPosition[i]];
                    if (boardPosition[i] == humanPlayer) {
                        occupiedCells++;
                    }
                }

                if (occupiedCells == 2) {
                    // Two occupied cells out of a maximum three means a pending winning move!
                    for (int i = 0; i < 3; i++) {
                        if (gameState[winningPosition[i]] == 2) {
                            // This is the unoccupied cell that is needed to win!
                            bestMoveIndex = winningPosition[i];

                            break mainDefensiveLoop;
                        }
                    }
                }
            }
        }

        Log.d(TAG, "findBestMove: defense: " + bestMoveIndex);

        return bestMoveIndex;
    }

    private boolean isMoveUnplayed(ImageView pView) {
        boolean result = false;

        if (pView.getDrawable() == null) {
            result = true;
        }

        return result;
    }

    private void makeMove(ImageView pView) {
        pView.setTranslationY(-1000f);

        // Set image
        if (activePlayer == 0) {
            pView.setImageResource(R.drawable.yellow);
        } else {
            pView.setImageResource(R.drawable.red);
        }

        // change animation speed for red player (machine) if in machine mode
        if (playerMode == 1) {
            if (activePlayer == 0) {
                pView.animate().translationYBy(1000f).rotation(360).setDuration(500);
            } else {
                pView.animate().translationYBy(1000f).rotation(360).setDuration(1000);
            }
        } else {
            pView.animate().translationYBy(1000f).rotation(360).setDuration(500);
        }
        gameState[Integer.valueOf(pView.getTag().toString())] = activePlayer;
    }

    private void switchPlayer() {
        if (activePlayer == 0) {
            activePlayer = 1;
        } else {
            activePlayer = 0;
        }
    }

    private void resetGameCells() {
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] == 2) {
                ImageView img = (ImageView) findViewById(
                        R.id.activity_main).findViewWithTag(Integer.toString(i));

                img.setImageResource(R.drawable.x_icon_sm);
            }
        }

        for (int i = 0; i < gameState.length; i++) {
            gameState[i] = 2;
        }
    }

    private boolean didCurrentPlayerWin() {
        boolean result = false;

        for (int[] winningPosition : winningPositions) {
            if (gameState[winningPosition[0]] == gameState[winningPosition[1]]
                    && gameState[winningPosition[1]] == gameState[winningPosition[2]]
                    && gameState[winningPosition[2]] != 2)
            {
                result = true;
                break;
            }
        }

        return result;
    }

    private void processPlayerMove(ImageView pView) {
        if (!gameOver) {
            boolean unplayedMove = isMoveUnplayed(pView);

            if (unplayedMove) {
                makeMove(pView);

                boolean playerWon = didCurrentPlayerWin();
                if (playerWon) {
                    String winner = (activePlayer == 0 ? "Yellow" : "Red") + " has won!";
                    Toast.makeText(MainActivity.this, winner, Toast.LENGTH_LONG).show();

                    resetGameCells();

                    gameOver = true;
                }

                boolean unplayedCells = checkIfUnplayedCells();
                if (!gameOver && unplayedCells) {
                    // switch player and keep playing
                    switchPlayer();
                } else {
                    if (!playerWon) {
                        Toast.makeText(MainActivity.this,
                                "Game over. Draw.", Toast.LENGTH_SHORT).show();
                    }

                    gameOver = true;
                }

            } else {
                Toast.makeText(MainActivity.this,
                        "Cell occupied; please play another cell.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "The game is over, please start another game.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class CustomClickListener implements View.OnClickListener {
        @Override
        public void onClick(View pView) {
            ImageView view = (ImageView) pView;

            processPlayerMove(view);

            // If on machine mode, let the machine generate a move
            if (playerMode == 1 && !gameOver) {
                ImageView generatedView = generateMove();

                // once the machine has generated a move you'll want to check everything for
                // the machine move as well
                processPlayerMove(generatedView);
            }
        }
    }
}
