package com.example.mydpcproject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SetupManagementFragment extends Fragment {

  @Override
  public View onCreateView(
          LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.setup_management_fragment, container, false);

    RadioGroup radioGroup = view.findViewById(R.id.setup_options);
    view.findViewById(R.id.next_button).setOnClickListener(v -> {
      int selectedId = radioGroup.getCheckedRadioButtonId();
      Context context = getActivity();

      if (selectedId == R.id.setup_managed_profile) {
        Toast.makeText(context, "Selected: Work & personal", Toast.LENGTH_SHORT).show();
      } else if (selectedId == R.id.setup_device_owner) {
        Toast.makeText(context, "Selected: Work only", Toast.LENGTH_SHORT).show();
      }

      // Закрываем activity
      getActivity().finish();
    });

    return view;
  }
}
