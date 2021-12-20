package br.com.fatec.projetoOrdensDeServicos.util;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;


public class MascaraMonetaria implements TextWatcher {
    private final WeakReference<TextInputEditText> editTextWeakReference;
    private final Locale locale;

    public MascaraMonetaria(TextInputEditText textInputEditText, Locale locale) {
        this.editTextWeakReference = new WeakReference<>(textInputEditText);
        this.locale = locale != null ? locale : Locale.getDefault();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        TextInputEditText textInputEditText = editTextWeakReference.get();
        if (textInputEditText == null) return;
        textInputEditText.removeTextChangedListener(this);

        BigDecimal parsed = parseToBigDecimal(editable.toString(), locale);
        String formatted = NumberFormat.getCurrencyInstance(locale).format(parsed);

        textInputEditText.setText(formatted);
        textInputEditText.setSelection(formatted.length());
        textInputEditText.addTextChangedListener(this);
    }

    private BigDecimal parseToBigDecimal(String value, Locale locale) {
        String replaceable = String.format("[%s,.\\s]", Objects.requireNonNull(
                NumberFormat.getCurrencyInstance(locale).getCurrency()).getSymbol());

        String cleanString = value.replaceAll(replaceable, "");

        return new BigDecimal(cleanString).setScale(
                2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR
        );
    }
}


