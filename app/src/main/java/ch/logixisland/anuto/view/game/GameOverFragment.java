package ch.logixisland.anuto.view.game;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import ch.logixisland.anuto.AnutoApplication;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.game.GameFactory;
import ch.logixisland.anuto.game.business.GameManager;
import ch.logixisland.anuto.game.theme.ThemeManager;

public class GameOverFragment extends Fragment implements GameManager.OnGameStartedListener,
        GameManager.OnGameOverListener {

    private final ThemeManager mThemeManager;
    private final GameManager mGameManager;

    private TextView txt_game_over;
    private TextView txt_score;

    public GameOverFragment() {
        GameFactory factory = AnutoApplication.getInstance().getGameFactory();
        mThemeManager = factory.getThemeManager();
        mGameManager = factory.getGameManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_over, container, false);

        txt_game_over = (TextView)v.findViewById(R.id.txt_game_over);
        txt_score = (TextView)v.findViewById(R.id.txt_score);

        txt_game_over.setTextColor(mThemeManager.getTheme().getTextColor());
        txt_score.setTextColor(mThemeManager.getTheme().getTextColor());
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        getFragmentManager().beginTransaction()
                .hide(this)
                .commit();

        mGameManager.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mGameManager.removeListener(this);
    }

    @Override
    public void onGameStarted() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(this)
                .commit();
    }

    @Override
    public void onGameOver() {
        txt_game_over.post(new Runnable() {
            @Override
            public void run() {
                if (mGameManager.isGameWon()) {
                    txt_game_over.setText(R.string.game_over_won);
                } else {
                    txt_game_over.setText(R.string.game_over_lost);
                }

                DecimalFormat fmt = new DecimalFormat("###,###,###,###");
                txt_score.setText(getResources().getString(R.string.score) +
                        ": " + fmt.format(mGameManager.getScore()));
            }
        });

        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .show(this)
                .commit();
    }
}
