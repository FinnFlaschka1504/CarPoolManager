package finn_daniel.carpoolmanager;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomDialog {
    enum buttonType {
        YES_NO, BACK, CUSTOM
    }

    public static final String YES_BUTTON = "YES_BUTTON";
    public static final String NO_BUTTON = "NO_BUTTON";
    public static final String BACK_BUTTON = "BACK_BUTTON";

    private static Button dialog_custom_B1;
    private static Button dialog_custom_B2;
    private static Button dialog_custom_B3;
    private static Button dialog_custom_B4;
    private static Button dialog_custom_B5;
    private static List<Button> buttons = new ArrayList<>();
    private static Dialog dialog;
    private static Context context;


    public static Dialog generateCustomDialog(Context context) {
        CustomDialog.context = context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_custom);


        return dialog;
    }

    public static Dialog showCustomDialog(Dialog dialog, String title, String text, buttonType buttonType,
                                          List<Pair<String, View.OnClickListener>> onClickListenerMap) {

        dialog_custom_B1 = dialog.findViewById(R.id.dialog_custom_Button1);
        dialog_custom_B2 = dialog.findViewById(R.id.dialog_custom_Button2);
        dialog_custom_B3 = dialog.findViewById(R.id.dialog_custom_Button3);
        dialog_custom_B4 = dialog.findViewById(R.id.dialog_custom_Button4);
        dialog_custom_B5 = dialog.findViewById(R.id.dialog_custom_Button5);

        buttons = Arrays.asList(dialog_custom_B1, dialog_custom_B2, dialog_custom_B3, dialog_custom_B4, dialog_custom_B5);

        setTexts(title, text, dialog);
        setButtonsType(buttonType, onClickListenerMap);
        setButtonsListener(buttonType, onClickListenerMap);

        setDialogLayoutParameters(dialog,true, false);

        return dialog;
    }

    private static void setTexts(String title, String text, Dialog dialog) {
        ((TextView) dialog.findViewById(R.id.dialog_custom_title)).setText(title);
        ((TextView) dialog.findViewById(R.id.dialog_custom_text)).setText(text);
    }

    private static void setButtonsType(buttonType buttonType, List<Pair<String, View.OnClickListener>> onClickListeners) {
        switch (buttonType) {
            case YES_NO:
                buttons.get(0).setVisibility(View.VISIBLE);
                buttons.get(0).setText("Ja");
                buttons.get(1).setVisibility(View.VISIBLE);
                buttons.get(1).setText("Nein");
                buttons.get(2).setVisibility(View.GONE);
                buttons.get(3).setVisibility(View.GONE);
                buttons.get(4).setVisibility(View.GONE);
                break;
            case BACK:
                buttons.get(0).setVisibility(View.VISIBLE);
                buttons.get(0).setText("Zurück");
                buttons.get(1).setVisibility(View.GONE);
                buttons.get(2).setVisibility(View.GONE);
                buttons.get(3).setVisibility(View.GONE);
                buttons.get(4).setVisibility(View.GONE);
                break;
            case CUSTOM:
                int count = 0;
                for (Pair<String, View.OnClickListener> listenerPair : onClickListeners) {
                    buttons.get(count).setText(listenerPair.first);
                    count++;
                }
                for (; count < 5; count++) {
                    buttons.get(count).setVisibility(View.GONE);
                }
                break;
        }
    }

    private static void setButtonsListener(buttonType buttonType, List<Pair<String, View.OnClickListener>> onClickListeners) {
        Map<String, View.OnClickListener> onClickListenerMap = new HashMap<>();
        for (Pair<String, View.OnClickListener> pair : onClickListeners) {
            onClickListenerMap.put(pair.first, pair.second);
        }
        switch (buttonType) {
            case YES_NO:
                if (onClickListenerMap.keySet().contains(YES_BUTTON)) {
                    buttons.get(0).setOnClickListener(onClickListenerMap.get(YES_BUTTON));
                }
                else
                    buttons.get(0).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                if (onClickListenerMap.keySet().contains(NO_BUTTON)) {
                    buttons.get(1).setOnClickListener(onClickListenerMap.get(NO_BUTTON));
                }
                else
                    buttons.get(1).setOnClickListener(view -> {
                        Toast.makeText(context, "Keine Funktion zugewisen", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                return;
            case BACK:
                if (onClickListenerMap.keySet().contains(BACK_BUTTON)) {
                    buttons.get(0).setOnClickListener(onClickListenerMap.get(BACK_BUTTON));
                }
                else
                    buttons.get(0).setOnClickListener(view -> dialog.dismiss());
                return;
        }

        if (onClickListeners == null)
            return;

        int count = 0;
        for (Pair<String, View.OnClickListener> listenerPair : onClickListeners) {
            buttons.get(count).setOnClickListener(listenerPair.second);
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
