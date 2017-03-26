package com.rakuishi.postalcode.view.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rakuishi.postalcode.R;
import com.rakuishi.postalcode.databinding.FragmentPostalCodeListBinding;
import com.rakuishi.postalcode.model.PostalCode;
import com.rakuishi.postalcode.repository.PostalCodeRepository;
import com.rakuishi.postalcode.view.activity.MainActivity;
import com.rakuishi.postalcode.view.adapter.PostalCodeListAdapter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.rakuishi.postalcode.view.fragment.PostalCodeListFragment.Type.CITY;
import static com.rakuishi.postalcode.view.fragment.PostalCodeListFragment.Type.STREET;

public class PostalCodeListFragment extends BaseFragment implements PostalCodeListAdapter.Callback {

    public enum Type {
        PREFECTURE, CITY, STREET;
    }

    private final static String TYPE = "type";
    private final static String ID = "id";
    private FragmentPostalCodeListBinding binding;
    private PostalCodeListAdapter adapter;
    private Type type;
    private int id;
    @Inject
    PostalCodeRepository postalCodeRepository;
    @Inject
    CompositeDisposable compositeDisposable;

    public static PostalCodeListFragment newInstance(Type type, int id) {
        PostalCodeListFragment fragment = new PostalCodeListFragment();
        Bundle args = new Bundle();
        args.putSerializable(TYPE, type);
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public static PostalCodeListFragment newInstance(Type type) {
        return newInstance(type, 0);
    }

    public PostalCodeListFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        appComponent().inject(this);
        type = (Type) getArguments().getSerializable(TYPE);
        id = getArguments().getInt(ID);
        Timber.d("type: " + type + ", id: " + id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_postal_code_list, container, false);
        adapter = new PostalCodeListAdapter(getContext(), this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Single<List<PostalCode>> single;
        switch (type) {
            case PREFECTURE:
                single = postalCodeRepository.findPrefectures();
                break;
            case CITY:
                single = postalCodeRepository.findByPrefectureId(id);
                break;
            case STREET:
            default:
                single = postalCodeRepository.findByCityId(id);
                break;
        }

        Disposable disposable = single
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((postalCodes, throwable) -> {
                    if (throwable == null) {
                        adapter.addAll(postalCodes);
                    }
                    binding.progressBar.setVisibility(View.GONE);
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    // region PostalCodeListAdapter.Callback

    @Override
    public void onItemClick(PostalCode postalCode) {
        Fragment fragment = null;
        switch (type) {
            case PREFECTURE:
                fragment = newInstance(CITY, postalCode.prefectureId);
                break;
            case CITY:
                // TODO: Support blank city pattern
                fragment = newInstance(STREET, postalCode.cityId);
                break;
            case STREET:
                // TODO: Add PostalCodeDetailFragment
                break;
        }

        if (fragment != null && getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(fragment);
        }
    }

    // endregion
}
