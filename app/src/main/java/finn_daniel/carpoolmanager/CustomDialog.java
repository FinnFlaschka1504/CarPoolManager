package finn_daniel.carpoolmanager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialog {
    public enum buttonType_Enum {
        YES_NO, SAVE_CANCEL, BACK, CUSTOM
    }

    private Context context;
    private Dialog dialog;
    private String title;
    private String text;
    private View view;
    private buttonType_Enum buttonType = buttonType_Enum.YES_NO;
    private Pair<Boolean, Boolean> dimensions = new Pair<>(true, false);
    private boolean dividerVisibility = true;
    private boolean isDividerVisibilityCustom = false;
    private int titleTextAlignment = View.TEXT_ALIGNMENT_CENTER;
    private boolean isTextBold = false;

    private List<Boolean> dismissDialogList = new ArrayList<>();
    private List<Pair<String, OnClick>> pairList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<Button> buttonList = new ArrayList<>();
    private List<Integer> buttonIdList = new ArrayList<>();

    public static final String YES_BUTTON = "YES_BUTTON";
    public static final String NO_BUTTON = "NO_BUTTON";
    public static final String SAVE_BUTTON = "SAVE_BUTTON";
    public static final String CANCEL_BUTTON = "CANCEL_BUTTON";
    public static final String BACK_BUTTON = "BACK_BUTTON";

    // ToDo: eventuell dialoginteraktionen auch als Executable

    public CustomDialog(Context context) {
        this.context = context;
    }

    public static CustomDialog Builder(Context context) {
        CustomDialog customDialog = new CustomDialog(context);
        return customDialog;
    }



    public CustomDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public CustomDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomDialog setText(String text) {
        this.text = text;
        return this;
    }

    public CustomDialog setView(int layoutId) {
        LayoutInflater li = LayoutInflater.from(context);
        this.view = li.inflate(layoutId, null);
        return this;
    }

    public CustomDialog setView(View view) {
        this.view = view;
        return this;
    }

    public CustomDialog setButtonType(buttonType_Enum buttonType) {
        this.buttonType = buttonType;
        return this;
    }

    public CustomDialog addButton(String buttonName, OnClick onClick) {
        return addButton_complete(buttonName, onClick, -1, true);
    }
    public CustomDialog addButton(String buttonName, OnClick onClick, int buttonId) {
        return addButton_complete(buttonName, onClick, buttonId, true);
    }

    public CustomDialog addButton(String buttonName, OnClick onClick, boolean dismissDialog){
        return addButton_complete(buttonName, onClick, -1, dismissDialog);
    }
    public CustomDialog addButton(String buttonName, OnClick onClick, int buttonId, boolean dismissDialog){
        return addButton_complete(buttonName, onClick, buttonId, dismissDialog);
    }

    private CustomDialog addButton_complete(String buttonName, OnClick onClick, int buttonId, boolean dismissDialog) {
        Pair<String, OnClick> pair = new Pair<>(buttonName, onClick);
        dismissDialogList.add(dismissDialog);
        pairList.add(pair);
        nameList.add(pair.first);
        buttonIdList.add(buttonId);
        return this;
    }

    public interface OnClick {
        void run(Dialog dialog);
    }
    

    public CustomDialog setDimensions(boolean width, boolean height) {
        this.dimensions = new Pair<>(width, height);
        return this;
    }

    public CustomDialog setDividerVisibility(boolean dividerVisibility) {
        this.dividerVisibility = dividerVisibility;
        isDividerVisibilityCustom = true;
        return this;
    }

    public CustomDialog setTitleTextAlignment(int titleTextAlignment) {
        this.titleTextAlignment = titleTextAlignment;
        return this;
    }

    public CustomDialog setTextBold(boolean textBold) {
        isTextBold = textBold;
        return this;
    }

    public Dialog show() {
        dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.dialog_custom);

        TextView dialog_custom_title = dialog.findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_text = dialog.findViewById(R.id.dialog_custom_text);

        dialog_custom_title.setTextAlignment(titleTextAlignment);

        if (isDividerVisibilityCustom) {
            dialog.findViewById(R.id.dialog_custom_divider1).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider2).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider3).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
            dialog.findViewById(R.id.dialog_custom_divider4).setVisibility(dividerVisibility ? View.VISIBLE : View.GONE);
        }

        if (title != null)
            dialog_custom_title.setText(this.title);
        else
            dialog_custom_title.setVisibility(View.GONE);

        if (text != null) {
            dialog_custom_text.setText(this.text);
            if (isTextBold)
                dialog_custom_text.setTypeface(null, Typeface.BOLD);
        }
        else
            dialog.findViewById(R.id.dialog_custom_layout_text).setVisibility(View.GONE);

        if (view != null)
            ((LinearLayout) dialog.findViewById(R.id.dialog_custom_layout_view_interface)).addView(view);
        else
            dialog.findViewById(R.id.dialog_custom_layout_view).setVisibility(View.GONE);

        if (text != null && view != null) {
            LinearLayout dialog_custom_layout_view = dialog.findViewById(R.id.dialog_custom_layout_view);
            dialog_custom_layout_view.setPadding(dialog_custom_layout_view.getPaddingLeft(), 0,
                    dialog_custom_layout_view.getPaddingRight(), dialog_custom_layout_view.getPaddingBottom());
            dialog.findViewById(R.id.dialog_custom_divider3).setVisibility(View.GONE);
        }

        if (text == null && view == null) {
            if (isDividerVisibilityCustom && dividerVisibility)
                dialog.findViewById(R.id.dialog_custom_divider).setVisibility(View.VISIBLE);
        }


            setDialogLayoutParameters(dialog, dimensions.first, dimensions.second);
        setButtons();
        setOnClickListeners();
        return dialog;

    }

    private void setButtons() {
        switch (buttonType) {
            case YES_NO:
                addNewButton("Nein");
                addNewButton("Ja");
                break;
            case SAVE_CANCEL:
                addNewButton("Abbrechen");
                addNewButton("Speichern");
                break;
            case BACK:
                addNewButton("Zurück");
                break;
            case CUSTOM:
                for (Pair<String, OnClick> pair : pairList) addNewButton(pair.first);
                break;
        }

    }

    private void addNewButton(String text) {
        Button button = new Button(context);
        button.setBackground(dialog.findViewById(R.id.dialog_custom_Button1).getBackground().getConstantState().newDrawable());
        button.setTextColor(((Button)dialog.findViewById(R.id.dialog_custom_Button1)).getTextColors());
        button.setText(text);
        ((FlowLayout) dialog.findViewById(R.id.dialog_custom_buttonLayout)).addView(button);
        buttonList.add(button);
    }

    private void setOnClickListeners() {
        Map<String, OnClick> stringOnClickMap = new HashMap<>();
        for (Pair<String, OnClick> pair : pairList) stringOnClickMap.put(pair.first, pair.second);

        switch (buttonType) {
            case YES_NO:
                if (stringOnClickMap.keySet().contains(NO_BUTTON)) {
                    int index = nameList.indexOf(NO_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run(dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                if (stringOnClickMap.keySet().contains(YES_BUTTON)) {
                    int index = nameList.indexOf(YES_BUTTON);
                    buttonList.get(1).setId(buttonIdList.get(index));
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run(dialog);
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                return;
            case SAVE_CANCEL:
                if (stringOnClickMap.keySet().contains(CANCEL_BUTTON)) {
                    int index = nameList.indexOf(CANCEL_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run(dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                if (stringOnClickMap.keySet().contains(SAVE_BUTTON)) {
                    int index = nameList.indexOf(SAVE_BUTTON);
                    buttonList.get(1).setId(buttonIdList.get(index));
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run(dialog);
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                return;

            case BACK:
                if (stringOnClickMap.keySet().contains(BACK_BUTTON)) {
                    int index = nameList.indexOf(BACK_BUTTON);
                    buttonList.get(0).setId(buttonIdList.get(index));
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run(dialog);
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                return;
        }

        int count = 0;
        for (Pair<String, OnClick> pair : pairList) {
            int finalCount = count;
            buttonList.get(count).setOnClickListener(view1 -> {
                if (dismissDialogList.get(finalCount))
                    dialog.dismiss();
                pair.second.run(dialog);
            });
            count++;
        }
    }

    static void setDialogLayoutParameters(Dialog dialog, boolean width, boolean height) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        if (width)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        else
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        if (height)
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        else
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
 }


