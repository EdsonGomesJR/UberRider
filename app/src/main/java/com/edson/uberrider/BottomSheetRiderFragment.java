package com.edson.uberrider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetRiderFragment  extends BottomSheetDialogFragment {

    String mLocation, mDestination;

    public static BottomSheetDialogFragment newInstance(String location, String destination)
    {

        BottomSheetDialogFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location",location);
        args.putString("destination",destination);
        f.setArguments(args);
        return f;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.bottom_sheet_rider,container,false);

       return view;
    }
}
