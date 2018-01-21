package com.froura.develo4.passenger.libraries;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.froura.develo4.passenger.HomeActivity;
import com.froura.develo4.passenger.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by KendrickCosca on 12/30/2017.
 */

public class DropOffAutoCompleteFragment extends PlaceAutocompleteFragment {

    private TextView textView;
    private ImageButton iconBtn;
    private ImageButton clearBtn;

    @Nullable
    private LatLngBounds latLngBounds;
    @Nullable
    private AutocompleteFilter autocompleteFilter;
    @Nullable
    private PlaceSelectionListener placeSelectionListener;

    public DropOffAutoCompleteFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dropoff_view, container, false);

        iconBtn = view.findViewById(R.id.icon);
        clearBtn = view.findViewById(R.id.clear);
        textView = view.findViewById(R.id.txtVw_dropoff);
        textView.setSelected(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIntent();
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Where are you going?");
                textView.setTextColor(getResources().getColor(R.color.place_autocomplete_search_text));
                setClear();
            }
        });
        iconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIntent();
            }
        });
        this.setClear();
        return view;
    }

    public void onDestroyView() {
        this.iconBtn = null;
        this.clearBtn = null;
        this.textView = null;
        super.onDestroyView();
    }

    public void setBoundsBias(@Nullable LatLngBounds bounds) {
        this.latLngBounds = bounds;
    }

    public void setFilter(@Nullable AutocompleteFilter filter) {
        this.autocompleteFilter = filter;
    }


    public void setText(CharSequence text) {
        this.textView.setText(text);
        this.textView.setTextColor(getResources().getColor(R.color.place_autocomplete_search_text));
        this.setClear();
    }

    public void setOnPlaceSelectedListener(PlaceSelectionListener listener) {
        this.placeSelectionListener = listener;
    }

    private void setClear() {
        boolean var1 = !this.textView.getText().toString().isEmpty();
        this.clearBtn.setVisibility(var1?View.VISIBLE:View.GONE);
    }

    private void showIntent() {
        int var1 = -1;

        try {
            Intent var2 = (new PlaceAutocomplete.IntentBuilder(2)).setBoundsBias(this.latLngBounds).setFilter(this.autocompleteFilter).zzih(this.textView.getText().toString()).zzea(1).build(this.getActivity());
            this.startActivityForResult(var2, 1);
        } catch (GooglePlayServicesRepairableException var3) {
            var1 = var3.getConnectionStatusCode();
            Log.e("Places", "Could not open autocomplete activity", var3);
        } catch (GooglePlayServicesNotAvailableException view) {
            var1 = view.errorCode;
            Log.e("Places", "Could not open autocomplete activity", view);
        }

        if (var1 != -1) {
            GoogleApiAvailability var5 = GoogleApiAvailability.getInstance();
            var5.showErrorDialogFragment(this.getActivity(), var1, 2);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                Place view = PlaceAutocomplete.getPlace(this.getActivity(), data);
                if (this.placeSelectionListener != null) {
                    this.placeSelectionListener.onPlaceSelected(view);
                }

                this.setText(view.getName().toString());
            } else if (resultCode == 2) {
                Status var5 = PlaceAutocomplete.getStatus(this.getActivity(), data);
                if (this.placeSelectionListener != null) {
                    this.placeSelectionListener.onError(var5);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
