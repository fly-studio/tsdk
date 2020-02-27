package org.fly.tsdk.sdk.view;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import org.fly.tsdk.sdk.exceptions.BindActivityException;

abstract public class BaseDialogFragment extends DialogFragment {

    @Override
    public void show(FragmentManager manager, String tag) {
        manager.beginTransaction().addToBackStack(null);
        super.show(manager, tag);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        transaction.addToBackStack(null);
        return super.show(transaction, tag);
    }

    public synchronized void show(Activity activity)
    {
        if (activity.getFragmentManager().findFragmentByTag(getFragmentTag()) != this)
            show(activity.getFragmentManager(), getFragmentTag());
    }

    public synchronized void hide()
    {
        dismiss();
    }

    public void attachToActivity(Activity activity)
    {
        if (getActivity() != null)
        {
            throw new BindActivityException("This fragment is in another activity.");
        }

        android.app.FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(getFragmentTag()) == null) {
            manager.beginTransaction()
                    .add(this, getFragmentTag())
                    //.hide(this)
                    .addToBackStack(null)
                    .commit();
            // Hopefully, we are the first to make a transaction.
            manager.executePendingTransactions();
        }
    }

    public void dettachFromActivity()
    {
        if (getActivity() != null)
        {
            dismiss();
        }
    }

    abstract public String getFragmentTag();
}
