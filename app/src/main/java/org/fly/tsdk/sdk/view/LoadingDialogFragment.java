package org.fly.tsdk.sdk.view;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.fly.tsdk.io.ResourceHelper;

public class LoadingDialogFragment extends BaseDialogFragment {
    private static final String LOADING_FRAGMENT_TAG = "tsdk_loading_fragment_tag";

    private TextView textView;

    public static LoadingDialogFragment getInstance(Context context)
    {
        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.setStyle(
                DialogFragment.STYLE_NO_FRAME,
                ResourceHelper.getId(context, "tsdk_full_fragment", ResourceHelper.DefType.STYLE)
        );
        return loadingDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(ResourceHelper.getId(getActivity(), "tsdk_loading", ResourceHelper.DefType.LAYOUT), container, false);
        textView = view.findViewById(ResourceHelper.getId(getActivity(), "tsdk_loading_message", ResourceHelper.DefType.ID));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        if (getDialog() != null && getDialog().getWindow() != null)
        {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            //params.dimAmount = 0;
            //params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;

            window.setBackgroundDrawableResource(ResourceHelper.getId(getActivity(), "tsdk_dialog_fragment_bg", ResourceHelper.DefType.DRAWABLE));
            window.setAttributes((WindowManager.LayoutParams) params);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        super.onResume();
    }

    public void setMessage(String message)
    {
//        textView.setText(message);
    }

    @Override
    public String getFragmentTag() {
        return LOADING_FRAGMENT_TAG;
    }
}
