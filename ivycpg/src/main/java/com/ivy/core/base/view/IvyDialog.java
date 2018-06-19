package com.ivy.core.base.view;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

public class IvyDialog {
    public final static String OK ="Ok";
    public final static String CANCEL ="Cancel";

    public IvyDialog() {
    }

    public static AlertDialog alertDialog(Context context, final int id, String title, String message, String buttonText, boolean cancelable,
                                          final IvyDialog.DialogListener listener) {
        AlertDialog dialog = null;

        try {
            dialog = (new AlertDialog.Builder(context)).setTitle(title).setMessage(message).setCancelable(cancelable).setPositiveButton(buttonText, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(listener != null) {
                        dialog.dismiss();
                        listener.alertDialogAction(IvyDialog.Action.OK, Integer.valueOf(id));
                    }

                }
            }).show();
            dialog.setCanceledOnTouchOutside(cancelable);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return dialog;
    }

    public static AlertDialog alertDialogOnly(Context context, String title, String message, String buttonText) {
        return alertDialog(context, -1, title, message, buttonText, false, null);
    }


    public static AlertDialog alertDialogOnly(Context context, String title, String message) {
        return alertDialogOnly(context, title, message, "OK");
    }

    public static AlertDialog alertDialogWithoutTitle(Context context, String message, String neutralButton) {
        return alertDialogOnly(context, null, message, neutralButton);
    }

    public static AlertDialog alertDialogSingleChoiceItem(Context context, final int id, String title, CharSequence[] items, final IvyDialog.DialogListener listener) {
        AlertDialog dialog = null;

        try {
            dialog = (new AlertDialog.Builder(context)).setTitle(title).setItems(items, new OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if(listener != null) {
                        listener.alertDialogAction(IvyDialog.Action.OK, Integer.valueOf(id), Integer.valueOf(item));
                        dialog.dismiss();
                    }

                }
            }).show();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return dialog;
    }

    public static AlertDialog alertDialog(Context context, final int id, String title, String message, String positiveBtnText, String negativeBtnText, boolean cancelable, final IvyDialog.DialogListener listener) {
        AlertDialog dialog = null;

        try {
            dialog = (new AlertDialog.Builder(context)).setTitle(title).setMessage(message).setCancelable(cancelable).setPositiveButton(positiveBtnText, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(listener != null) {
                        listener.alertDialogAction(IvyDialog.Action.OK, Integer.valueOf(id));
                        dialog.dismiss();
                    }

                }
            }).setNegativeButton(negativeBtnText, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(listener != null) {
                        listener.alertDialogAction(IvyDialog.Action.CANCEL, new Object[]{Integer.valueOf(id)}, dialog);
                        dialog.dismiss();
                    }

                }
            }).show();
            dialog.setCanceledOnTouchOutside(cancelable);
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        return dialog;
    }

    /**
     * Implement the views by getting dialog.
     * @param context - context
     * @param id - date reference
     * @param title -Title
     * @param message - message
     * @param positiveBtnText - Ok
     * @param negativeBtnText - cancel
     * @param cancelable - iscancelable
     * @param layoutid - Layout to show
     * @param listener - listener for buttons
     *
     * @return - Dilaog
     */
    public static AlertDialog alertDialogWithView(Context context, final int id, String title, String message, String positiveBtnText, String negativeBtnText, boolean cancelable, int layoutid, final IvyDialog.DialogListener listener) {
        AlertDialog dialog = null;

        try {
            dialog = (new AlertDialog.Builder(context)).setTitle(title).setCancelable(cancelable)
                    .setPositiveButton(positiveBtnText, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(listener != null) {
                                listener.alertDialogAction(IvyDialog.Action.OK, Integer.valueOf(id));
                                dialog.dismiss();
                            }

                        }
                    }).setNegativeButton(negativeBtnText, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(listener != null) {
                                listener.alertDialogAction(IvyDialog.Action.CANCEL, new Object[]{Integer.valueOf(id)}, dialog);
                                dialog.dismiss();
                            }

                        }
                    }).show();
            dialog.setContentView(layoutid);
            dialog.setCanceledOnTouchOutside(cancelable);
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        return dialog;
    }

    public interface DialogListener {
        void alertDialogAction(IvyDialog.Action var1, Object... var2);
    }

    public enum Action {
        OK,
        CANCEL;

        Action() {
        }
    }
}
