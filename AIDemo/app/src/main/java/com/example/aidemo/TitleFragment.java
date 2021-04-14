package com.example.aidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aidemo.databinding.FragmentTitleBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TitleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TitleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TitleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TitleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TitleFragment newInstance(String param1, String param2) {
        TitleFragment fragment = new TitleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
//        return inflater.inflate(R.layout.fragment_title, container, false);
        FragmentTitleBinding binding;
        binding  = DataBindingUtil.inflate(inflater,R.layout.fragment_title,container,false);
        binding.setLifecycleOwner(requireActivity());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button1:
                        NavController controller = Navigation.findNavController(view);
                        controller.navigate(R.id.action_titleFragment_to_classificationFragment);
                        break;
                    case R.id.button2:
                        NavController controller2 = Navigation.findNavController(view);
                        controller2.navigate(R.id.action_titleFragment_to_objectDetectionFragment);
                        break;
                    case R.id.button3:
                        NavController controller3 = Navigation.findNavController(view);
                        controller3.navigate(R.id.action_titleFragment_to_superResolutionFragment);
                        break;
                    case R.id.button4:
                        NavController controller4 = Navigation.findNavController(view);
                        controller4.navigate(R.id.action_titleFragment_to_styleTransferFragment);
                        break;
                    case R.id.button5:
                        SurfaceActivity.USE_MODEL = SurfaceActivity.FACE_LANDMARK;
                        NavController controller5 = Navigation.findNavController(view);
                        controller5.navigate(R.id.action_titleFragment_to_surfaceActivity);
                        break;
                    case R.id.button6:
                        SurfaceActivity.USE_MODEL = SurfaceActivity.SIMPLE_POSE;
                        NavController controller6 = Navigation.findNavController(view);
                        controller6.navigate(R.id.action_titleFragment_to_surfaceActivity);
                        break;
                }
            }
        };

        binding.button1.setOnClickListener(listener);
        binding.button2.setOnClickListener(listener);
        binding.button3.setOnClickListener(listener);
        binding.button4.setOnClickListener(listener);
        binding.button5.setOnClickListener(listener);
        binding.button6.setOnClickListener(listener);
        return binding.getRoot();
    }
}