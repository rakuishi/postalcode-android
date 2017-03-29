package com.rakuishi.postalcode.view.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toolbar;

import com.rakuishi.postalcode.R;
import com.rakuishi.postalcode.databinding.ActivityPostalCodeBinding;
import com.rakuishi.postalcode.view.fragment.PostalCodeDetailFragment;
import com.rakuishi.postalcode.view.fragment.PostalCodeListFragment;

import java.util.ArrayList;
import java.util.List;

public class PostalCodeActivity extends BaseActivity {

    public enum Type {
        CITY, STREET, DETAIL
    }

    private final static String TYPE = "type";
    private final static String ID = "id";
    private final static String TITLE = "title";
    private ActivityPostalCodeBinding binding;
    private List<String> fragmentNames = new ArrayList<>();

    public static Intent newInstance(Context context, Type type, int id, String title) {
        Intent intent = new Intent(context, PostalCodeActivity.class);
        intent.putExtra(TYPE, type);
        intent.putExtra(ID, id);
        intent.putExtra(TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_postal_code);
        appComponent().inject(this);
        setSupportActionBar(binding.view.toolbar);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(TYPE) || !intent.hasExtra(ID)) {
            throw new IllegalStateException("PostalCodeActivity requires type and id parameters.");
        }

        Type type = (Type) intent.getSerializableExtra(TYPE);
        int id = intent.getIntExtra(ID, 0);
        String title = intent.hasExtra(TITLE) ? intent.getStringExtra(TITLE) : "";
        Fragment fragment = null;

        switch (type) {
            case CITY:
                fragment = PostalCodeListFragment.newInstance(PostalCodeListFragment.Type.CITY, id);
                replaceFragment(fragment, title);
                break;
            case STREET:
                fragment = PostalCodeListFragment.newInstance(PostalCodeListFragment.Type.STREET, id);
                replaceFragment(fragment, title);
                break;
            case DETAIL:
                // FIXME: Add a new method to make a new instance
                fragment = PostalCodeDetailFragment.newInstance("");
                replaceFragment(fragment, title);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (fragmentNames.size() > 1) {
            manager.popBackStack();
            fragmentNames.remove(fragmentNames.size() - 1);
            updateActionBar();
            return;
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void replaceFragment(@NonNull Fragment fragment, @NonNull String title) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        fragmentNames.add(title);
        updateActionBar();
    }

    public void updateActionBar() {
        if (getSupportActionBar() != null && fragmentNames.size() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(fragmentNames.get(fragmentNames.size() - 1));
        }
    }
}
