package com.adgad.kboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * Created by arjun on 03/01/16.
 */
public class AddWordDialogFragment extends DialogFragment {

    private static final int[] PALETTE = {
            R.color.md_red500,
            R.color.md_pink500,
            R.color.md_purple500,
            R.color.md_indigo500,
            R.color.md_blue500,
            R.color.md_cyan500,
            R.color.md_teal500,
            R.color.md_green500,
            R.color.md_orange500,
            R.color.md_deeporange500,
            R.color.md_yellow500,
            R.color.md_brown500,
            R.color.md_red200,
            R.color.md_pink200,
            R.color.md_blue200,
            R.color.md_teal200,
            R.color.md_green200,
            R.color.md_yellow200,
            R.color.md_black,
            R.color.md_white,
            R.color.md_grey700,
            R.color.md_bluegrey500
    };

    public interface AddWordDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int index);
        void onDialogNegativeClick(DialogFragment dialog, int index);
        void onDialogNeutralClick(int index);


    }


    // Use this instance of the interface to deliver action events
    private AddWordDialogListener mListener;
    private Integer selectedColor;
    private TextView preview;
    private final List<TextView> swatches = new ArrayList<TextView>();
    private final List<Integer> swatchColors = new ArrayList<Integer>();

    //Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddWordDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddWordDialog");
        }
    }

    public Integer getSelectedColor() {
        return selectedColor;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        String existingWord = getArguments().getString("word");
        View view = inflater.inflate(R.layout.dialog_new_word, null);
        if(existingWord != null) {
            ((TextView)view.findViewById(R.id.word)).setText(existingWord);
        }
        if (getArguments().getBoolean("hasColor", false)) {
            selectedColor = getArguments().getInt("color");
        } else {
            selectedColor = null;
        }
        preview = (TextView) view.findViewById(R.id.color_preview);
        buildSwatches((GridLayout) view.findViewById(R.id.color_grid));
        final int index = getArguments().getInt("index");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setTitle("Edit Key")
                .setNeutralButton("Move up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNeutralClick(index);
                    }
                })
                .setPositiveButton(index > -1 ? "OK" : "Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AddWordDialogFragment.this, index);
                    }
                })
                .setNegativeButton(index > -1 ? "Delete" : "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AddWordDialogFragment.this, index);
                    }
                });
        return builder.create();
    }

    private void buildSwatches(GridLayout grid) {
        swatches.clear();
        swatchColors.clear();
        List<Integer> colors = new ArrayList<Integer>();
        colors.add(null);
        boolean found = selectedColor == null;
        for (int id : PALETTE) {
            int color = ContextCompat.getColor(getActivity(), id);
            colors.add(color);
            if (selectedColor != null && selectedColor == color) {
                found = true;
            }
        }
        if (!found) {
            colors.add(selectedColor);
        }
        for (int i = 0; i < colors.size(); i++) {
            final Integer color = colors.get(i);
            TextView swatch = new TextView(getActivity());
            int size = dp(32);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            int gap = dp(4);
            params.setMargins(gap, gap, gap, gap);
            swatch.setLayoutParams(params);
            swatch.setGravity(Gravity.CENTER);
            if (color == null) {
                swatch.setText("×");
                swatch.setTextColor(0xFF616161);
                swatch.setContentDescription("No color");
            } else {
                swatch.setContentDescription("Key color");
            }
            swatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColor = color;
                    if (getArguments() != null) {
                        if (color == null) {
                            getArguments().remove("color");
                            getArguments().putBoolean("hasColor", false);
                        } else {
                            getArguments().putBoolean("hasColor", true);
                            getArguments().putInt("color", color);
                        }
                    }
                    refreshSwatches();
                }
            });
            grid.addView(swatch);
            swatches.add(swatch);
            swatchColors.add(color);
        }
        refreshSwatches();
    }

    private void refreshSwatches() {
        for (int i = 0; i < swatches.size(); i++) {
            Integer color = swatchColors.get(i);
            boolean selected = color == null ? selectedColor == null : color.equals(selectedColor);
            swatches.get(i).setBackground(swatchDrawable(color, selected));
        }
        if (selectedColor == null) {
            preview.setText("Uses keyboard background");
            preview.setBackgroundColor(0xFFECEFF1);
            preview.setTextColor(0xFF212121);
        } else {
            preview.setText("Key preview");
            preview.setBackgroundColor(selectedColor);
            preview.setTextColor(ColorContrast.textColorFor(selectedColor));
        }
    }

    private GradientDrawable swatchDrawable(Integer color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        if (color == null) {
            drawable.setColor(0xFFEEEEEE);
        } else {
            drawable.setColor(color);
        }
        int stroke = color == null ? 0xFF9E9E9E : ColorContrast.textColorFor(color);
        if (!selected && color != null) {
            stroke = 0x33000000;
        }
        drawable.setStroke(dp(selected ? 3 : 1), stroke);
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
