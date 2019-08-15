package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public String inputText, resultDate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        SimpleDateFormat iosFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        inputText = "" + year + "-" + (month + 1) + "-" + day;
        Date date = null;
        try {
            date = simpleFormat.parse(inputText);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            resultDate = iosFormat.format(date);
        }
    }
}