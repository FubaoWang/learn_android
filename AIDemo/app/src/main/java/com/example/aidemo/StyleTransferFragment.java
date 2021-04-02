package com.example.aidemo;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aidemo.databinding.FragmentStyleTransferBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StyleTransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StyleTransferFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StyleTransferFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StyleTransferFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StyleTransferFragment newInstance(String param1, String param2) {
        StyleTransferFragment fragment = new StyleTransferFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private FragmentStyleTransferBinding binding;
    private Handler handler;
    private HandlerThread handlerThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_style_transfer, container, false);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_style_transfer,container,false);
        binding.setLifecycleOwner(requireActivity());
        initlistenr();
        return binding.getRoot();
    }
    void init(){}
    void initlistenr(){

    }
}