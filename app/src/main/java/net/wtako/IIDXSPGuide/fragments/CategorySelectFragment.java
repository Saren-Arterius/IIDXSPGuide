package net.wtako.IIDXSPGuide.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;

import net.wtako.IIDXSPGuide.R;
import net.wtako.IIDXSPGuide.activities.MainActivity;
import net.wtako.IIDXSPGuide.data.IIDXDifficultyLevel;
import net.wtako.IIDXSPGuide.data.IIDXVersion;
import net.wtako.IIDXSPGuide.interfaces.SelectionOption;
import net.wtako.IIDXSPGuide.utils.LollipopUtils;
import net.wtako.IIDXSPGuide.utils.MiscUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CategorySelectFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String PREF_SELECTION = "pref_selection";
    @Bind(R.id.category_grid)
    GridView gridView;
    private List<SelectionOption> options;

    public CategorySelectFragment() {
    }

    public static CategorySelectFragment newInstance(CategorySelection selection) {
        CategorySelectFragment frag = new CategorySelectFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PREF_SELECTION, selection.name());
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_select, container, false);
        ButterKnife.bind(this, rootView);
        QuickAdapter<SelectionOption> adapter;
        // This is to prevent java.lang.VerifyError
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adapter = new QuickAdapter<SelectionOption>(getActivity(), R.layout.grid_category_item) {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                protected void convert(BaseAdapterHelper helper, SelectionOption item) {

                    View gridContainer = helper.getView(R.id.category_item_container);
                    /*
                    int wPixel = MiscUtils.dpToPx(context, 112);
                    int hPixel = MiscUtils.dpToPx(context, 112);
                    gridContainer.setLayoutParams(new AbsListView.LayoutParams(wPixel, hPixel));
                    */
                    helper.setText(R.id.category_item_text, item.getDisplayName());
                    int color = item.getColor(getActivity());
                    int brighter = MiscUtils.getBrighterColor(color);
                    gridContainer.setBackground(LollipopUtils.getPressedColorRippleDrawable(color, brighter));
                }
            };
        } else {
            adapter = new QuickAdapter<SelectionOption>(getActivity(), R.layout.grid_category_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, SelectionOption item) {

                    View gridContainer = helper.getView(R.id.category_item_container);
                    /*
                    int wPixel = MiscUtils.dpToPx(context, 112);
                    int hPixel = MiscUtils.dpToPx(context, 112);
                    gridContainer.setLayoutParams(new ViewGroup.MarginLayoutParams(wPixel, hPixel));
                    */
                    helper.setText(R.id.category_item_text, item.getDisplayName());
                    int color = item.getColor(getActivity());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        int brighter = MiscUtils.getBrighterColor(color);
                        gridContainer.setBackground(MiscUtils.getPressedColorDrawable(color, brighter));
                    } else {
                        gridContainer.setBackgroundColor(color);
                    }
                }
            };
        }
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            gridView.setSelector(android.R.color.transparent);
        }
        CategorySelection selection = CategorySelection.valueOf(getArguments().getString(PREF_SELECTION));
        if (selection == CategorySelection.DIFFICULTY_LEVEL) {
            options = Arrays.<SelectionOption>asList(IIDXDifficultyLevel.values());
            if (getActivity() instanceof MainActivity) {
                MainActivity main = ((MainActivity) getActivity());
                main.getToolbar().setTitle(R.string.text_difficulty);
                main.animateAppAndStatusBar(main.getResources().getColor(R.color.material_red_400));
            }
        } else if (selection == CategorySelection.IIDX_VERSION) {
            List<SelectionOption> list = Arrays.<SelectionOption>asList(IIDXVersion.values());
            Collections.reverse(list);
            options = list;
            if (getActivity() instanceof MainActivity) {
                MainActivity main = ((MainActivity) getActivity());
                main.getToolbar().setTitle(R.string.text_iidx_version);
                main.animateAppAndStatusBar(main.getResources().getColor(R.color.material_teal_400));
            }
        }
        adapter.addAll(options);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onSelect(options.get(position));
        }
    }

    public enum CategorySelection {
        DIFFICULTY_LEVEL,
        IIDX_VERSION
    }

}
