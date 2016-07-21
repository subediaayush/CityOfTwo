package com.messenger.cityoftwo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.appyvet.rangebar.IRangeBarFormatter;
import com.appyvet.rangebar.RangeBar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aayush on 7/3/2016.
 */
public class FiltersFragment extends DialogFragment {

    Context context;
    Boolean enableFilters;
    Integer credits;
    Integer minimumAge;
    Integer maximumAge;
    Boolean matchMale;
    Boolean matchFemale;
    Boolean distanceInMiles;
    Integer maximumDistance;
    Integer floatingDistance;
    OnDialogEventListener mDialogEventListener;
    JSONObject mFilters;

    public static FiltersFragment newInstance() {
        Bundle args = new Bundle();

        FiltersFragment fragment = new FiltersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (context == null) context = getContext();

        View view = createView(context);

        return new AlertDialog.Builder(context)
                .setTitle("Apply Filters")
                .setView(view)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mFilters = new JSONObject()
                                    .put(CityOfTwo.KEY_MIN_AGE, minimumAge)
                                    .put(CityOfTwo.KEY_MAX_AGE, maximumAge)
                                    .put(CityOfTwo.KEY_DISTANCE, maximumDistance)
                                    .put(CityOfTwo.KEY_MATCH_MALE, matchMale)

                                    .put(CityOfTwo.KEY_MATCH_FEMALE, matchFemale);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (mDialogEventListener != null)
                            mDialogEventListener.OnFiltersApply(mFilters);
                    }
                })
                .create();
    }

    private View createView(final Context context) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_apply_filter, null);

        SharedPreferences securePreferences = new SecurePreferences(context, CityOfTwo.SECURED_PREFERENCE);
        SharedPreferences sharedPreferences = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE);

        credits = securePreferences.getInt(CityOfTwo.KEY_CREDITS, 0);
        Switch filterToggle = (Switch) view.findViewById(R.id.filter_enable_switch);

        enableFilters = sharedPreferences.getBoolean(CityOfTwo.KEY_FILTERS_APPLIED, false);
        if (credits == 0) {
            enableFilters = false;
            securePreferences.edit().putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, false).apply();
        }

        final View filtersContainer = view.findViewById(R.id.filters_container);
        filterToggle.setChecked(enableFilters);
        enableDisableView(filtersContainer, enableFilters);

//        final TextView minAge = (TextView) rootView.findViewById(R.id.filter_age_min);
//        final TextView maxAge = (TextView) rootView.findViewById(R.id.filter_age_max);

        final RangeBar ageRange = (RangeBar) view.findViewById(R.id.filter_age_range);
        ageRange.setDrawTicks(false);

        final Switch maleSwitch = (Switch) view.findViewById(R.id.filter_male_switch);
        final Switch femaleSwitch = (Switch) view.findViewById(R.id.filter_female_switch);

        final RangeBar distanceRange = (RangeBar) view.findViewById(R.id.filter_distance_bar);
        distanceRange.setDrawTicks(false);
//        final TextView distanceText = (TextView) rootView.findViewById(R.id.filter_distance_text);

        RadioGroup unitChooser = (RadioGroup) view.findViewById(R.id.filter_distance_unit);

        minimumAge = Math.max(
                securePreferences.getInt(CityOfTwo.KEY_MIN_AGE, 0),
                CityOfTwo.MINIMUM_AGE
        );
        maximumAge = Math.min(
                securePreferences.getInt(CityOfTwo.KEY_MAX_AGE, CityOfTwo.MAXIMUM_AGE),
                CityOfTwo.MAXIMUM_AGE
        );

        maximumDistance = Math.min(
                securePreferences.getInt(CityOfTwo.KEY_DISTANCE, CityOfTwo.MAXIMUM_DISTANCE),
                CityOfTwo.MAXIMUM_DISTANCE
        );
//        floatingDistance = Float.valueOf(maximumDistance);

        matchMale = securePreferences.getBoolean(CityOfTwo.KEY_MATCH_MALE, true);
        matchFemale = securePreferences.getBoolean(CityOfTwo.KEY_MATCH_FEMALE, true);

        distanceInMiles = securePreferences.getBoolean(CityOfTwo.KEY_DISTANCE_IN_MILES, false);

//        minAge.setText(String.valueOf(minimumAge));
//        maxAge.setText(String.valueOf(maximumAge));
        try {
            mFilters = new JSONObject()
                    .put(CityOfTwo.KEY_MIN_AGE, minimumAge)
                    .put(CityOfTwo.KEY_MAX_AGE, maximumAge)
                    .put(CityOfTwo.KEY_DISTANCE, maximumDistance)
                    .put(CityOfTwo.KEY_MATCH_MALE, matchMale)
                    .put(CityOfTwo.KEY_MATCH_FEMALE, matchFemale);
        } catch (JSONException e) {
            mFilters = new JSONObject();
        }


        floatingDistance = distanceInMiles ? maximumDistance * 5 : maximumDistance * 8;
        Log.i("Floating Distance", String.valueOf(floatingDistance));
        ageRange.setRangePinsByIndices(
                minimumAge - CityOfTwo.MINIMUM_AGE,
                maximumAge - CityOfTwo.MINIMUM_AGE
        );

        maleSwitch.setChecked(matchMale);
        femaleSwitch.setChecked(matchFemale);

        if (distanceInMiles) {
            unitChooser.check(R.id.distance_miles);
            distanceRange.setTickInterval(5);
        } else {
            unitChooser.check(R.id.distance_km);
            distanceRange.setTickInterval(8);
        }

//        distanceText.setText(String.valueOf(maximumDistance));
        distanceRange.setSeekPinByValue(floatingDistance);
        distanceRange.setFormatter(new IRangeBarFormatter() {
            @Override
            public String format(String value) {
                Integer shownDistance = Integer.parseInt(value) / (distanceInMiles ? 5 : 8);

                return String.valueOf((int) shownDistance);
            }
        });

        filterToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && credits == 0) {
                    showCreditsDialog();
                    buttonView.setChecked(false);
                } else {
                    enableFilters = isChecked;
                    enableDisableView(filtersContainer, enableFilters);
                    if (isChecked) {
                        minimumAge = CityOfTwo.MINIMUM_AGE;
                        maximumAge = CityOfTwo.MAXIMUM_AGE;
                        maximumDistance = CityOfTwo.MAXIMUM_DISTANCE;
                        matchMale = true;
                        matchFemale = true;
                    } else {
                        minimumAge = ageRange.getLeftIndex() + CityOfTwo.MINIMUM_AGE;
                        maximumAge = ageRange.getRightIndex() + CityOfTwo.MINIMUM_AGE;
                        maximumDistance = distanceRange.getRightIndex() * (distanceInMiles ? 5 : 8) / 8;
                        matchMale = maleSwitch.isChecked();
                        matchFemale = femaleSwitch.isChecked();
                    }
                }
            }
        });

        ageRange.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                minimumAge = leftPinIndex + CityOfTwo.MINIMUM_AGE;
                maximumAge = rightPinIndex + CityOfTwo.MINIMUM_AGE;

                Log.i("Age Filter", "Minimum age " + minimumAge + " Maximum age " + maximumAge);
            }
        });

        distanceRange.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                floatingDistance = rightPinIndex * (distanceInMiles ? 5 : 8);

                maximumDistance = floatingDistance / 8;

                Log.i("FloatingDistance", String.valueOf(floatingDistance));
                Log.i("MaximumDistance", String.valueOf(maximumDistance));
            }
        });

        unitChooser.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Integer currentIndex = distanceRange.getRightIndex();
                if (checkedId == R.id.distance_miles) {
                    distanceRange.setTickInterval(5);
                    distanceRange.setSeekPinByIndex(currentIndex * 8 / 5);
                    distanceInMiles = true;
                } else if (checkedId == R.id.distance_km) {
                    distanceRange.setTickInterval(8);
                    distanceInMiles = false;
                    distanceRange.setSeekPinByIndex(currentIndex * 5 / 8);
                }
            }
        });

        CompoundButton.OnCheckedChangeListener genderSwitchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.getId() == R.id.filter_male_switch)
                    matchMale = isChecked;
                else if (buttonView.getId() == R.id.filter_female_switch)
                    matchFemale = isChecked;
            }
        };
        maleSwitch.setOnCheckedChangeListener(genderSwitchListener);
        femaleSwitch.setOnCheckedChangeListener(genderSwitchListener);

        return view;
    }

    private void showCreditsDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Not enough credits")
                .setMessage("You'll need credits to apply filters")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDialogEventListener != null) mDialogEventListener.OnCoyRudyShared();
                    }
                })
                .show();
    }

    private Float getMiles(Float distanceKm) {
        return distanceKm / 1.6f;
    }

    private Float getKiloMeters(Float distanceMiles) {
        return distanceMiles * 1.6f;
    }

    private Float getMiles(Integer distanceKm) {
        return distanceKm / 1.6f;
    }

    private Float getKiloMeters(Integer distanceMiles) {
        return distanceMiles * 1.6f;
    }

    public void setOnDialogEventListener(OnDialogEventListener dialogEventListener) {
        mDialogEventListener = dialogEventListener;
    }

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

    public interface OnDialogEventListener {
        void OnFiltersApply(JSONObject filters);

        void OnCoyRudyShared();
    }
}
