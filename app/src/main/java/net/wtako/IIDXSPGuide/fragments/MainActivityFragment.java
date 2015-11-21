package net.wtako.IIDXSPGuide.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;

import butterknife.ButterKnife;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        if (getActivity() instanceof MainActivity) {
            MainActivity main = ((MainActivity) getActivity());
            main.getToolbar().setTitle(R.string.app_name);
            main.animateAppAndStatusBar(main.getResources().getColor(R.color.material_amber_500));
        }
        return rootView;
    }

}
