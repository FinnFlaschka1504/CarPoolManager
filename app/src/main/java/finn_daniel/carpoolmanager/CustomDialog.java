package finn_daniel.carpoolmanager;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialog {
    public enum buttonType_Enum {
        YES_NO, BACK, CUSTOM
    }

    private Context context;
    private Dialog dialog;
    private String title;
    private String text;
    private View view;
    private buttonType_Enum buttonType = buttonType_Enum.YES_NO;
    private Pair<Boolean, Boolean> dimensions = new Pair<>(true, false);

    private List<Boolean> dismissDialogList = new ArrayList<>();
    private List<Pair<String, Runnable>> pairList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<Button> buttonList = new ArrayList<>();

    public static final String YES_BUTTON = "YES_BUTTON";
    public static final String NO_BUTTON = "NO_BUTTON";
    public static final String BACK_BUTTON = "BACK_BUTTON";

    // ToDo: eventuell dialoginteraktionen auch als Executable
    // ToDo: auswählbar machen, ob divider angezeigt werden sollen, oder nicht

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

    public CustomDialog addButton(String buttonName, Runnable runnable) {
        Pair<String, Runnable> pair = new Pair<>(buttonName, runnable);
        dismissDialogList.add(true);
        pairList.add(pair);
        nameList.add(pair.first);
        return this;
    }
    public CustomDialog addButton(String buttonName, Runnable runnable, boolean dismissDialog){
        Pair<String, Runnable> pair = new Pair<>(buttonName, runnable);
        dismissDialogList.add(dismissDialog);
        pairList.add(pair);
        nameList.add(pair.first);
        return this;
    }

    public CustomDialog setDimensions(boolean width, boolean height) {
        this.dimensions = new Pair<>(width, height);
        return this;
    }

    public Dialog show() {
        dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.dialog_custom);

        TextView dialog_custom_title = dialog.findViewById(R.id.dialog_custom_title);
        TextView dialog_custom_text = dialog.findViewById(R.id.dialog_custom_text);

        if (title != null)
            dialog_custom_title.setText(this.title);
        else
            dialog_custom_title.setVisibility(View.GONE);

        if (text != null)
            dialog_custom_text.setText(this.text);
        else
            dialog_custom_text.setVisibility(View.GONE);

        if (view != null)
            ((LinearLayout) dialog.findViewById(R.id.dialog_custom_layout)).addView(view);
        
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
            case BACK:
                addNewButton("Zurück");
                break;
            case CUSTOM:
                for (Pair<String, Runnable> pair : pairList) addNewButton(pair.first);
                break;
        }

    }

    private void addNewButton(String text) {
        Button button = new Button(context);
        button.setBackground(dialog.findViewById(R.id.dialog_custom_Button1).getBackground().getConstantState().newDrawable());
        button.setText(text);
        button.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        ((FlowLayout) dialog.findViewById(R.id.dialog_custom_buttonLayout)).addView(button);
        buttonList.add(button);
    }

    private void setOnClickListeners() {
        Map<String, Runnable> stringRunnableMap = new HashMap<>();
        for (Pair<String, Runnable> pair : pairList) stringRunnableMap.put(pair.first, pair.second);

        switch (buttonType) {
            case YES_NO:
                if (stringRunnableMap.keySet().contains(NO_BUTTON)) {
                    int index = nameList.indexOf(NO_BUTTON);
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run();
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                if (stringRunnableMap.keySet().contains(YES_BUTTON)) {
                    int index = nameList.indexOf(YES_BUTTON);
                    buttonList.get(1).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run();
                    });
                }
                else
                    buttonList.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });

                return;
            case BACK:
                if (stringRunnableMap.keySet().contains(BACK_BUTTON)) {
                    int index = nameList.indexOf(BACK_BUTTON);
                    buttonList.get(0).setOnClickListener(view1 -> {
                        if (dismissDialogList.get(index))
                            dialog.dismiss();
                        pairList.get(index).second.run();
                    });
                }
                else
                    buttonList.get(0).setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                return;
        }

        int count = 0;
        for (Pair<String, Runnable> pair : pairList) {
            int finalCount = count;
            buttonList.get(count).setOnClickListener(view1 -> {
                if (dismissDialogList.get(finalCount))
                    dialog.dismiss();
                pair.second.run();
            });
            count++;
        }
    }

    static void setDialogLayoutParameters(Dialog dialog, boolean width, boolean height) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        if (width)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (height)
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}
