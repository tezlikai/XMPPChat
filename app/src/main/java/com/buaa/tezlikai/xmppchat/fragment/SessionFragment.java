package com.buaa.tezlikai.xmppchat.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buaa.tezlikai.xmppchat.R;

public class SessionFragment extends Fragment {

    public SessionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session, container, false);
    }

}
