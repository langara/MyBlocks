package pl.mareklangiewicz.myfragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import pl.mareklangiewicz.myviews.MyViewDecorator;


public final class MyDecoratorsTestsFragment extends MyFragment {

    public MyDecoratorsTestsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.my_decorators_tests_fragment, container, false);
    }

    //TODO: move menu to local menu (right side drawer)

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_decorators_options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_decorate_views) { // TODO later: change this to FAB action!!!
            View view = getView();
            if(view == null)
                throw new NullPointerException("The root view of this fragment is null.");
            MyViewDecorator.decorateTree(view, "decorate", R.layout.example_decoration, null);
            return true;
        }
        return false;
    }
}
