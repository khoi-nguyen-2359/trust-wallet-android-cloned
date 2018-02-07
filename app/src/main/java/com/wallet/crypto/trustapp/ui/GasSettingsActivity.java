package com.wallet.crypto.trustapp.ui;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wallet.crypto.trustapp.C;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.GasSettings;
import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.util.BalanceUtils;
import com.wallet.crypto.trustapp.viewmodel.GasSettingsViewModel;
import com.wallet.crypto.trustapp.viewmodel.GasSettingsViewModelFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class GasSettingsActivity extends BaseActivity {

    @Inject
    GasSettingsViewModelFactory viewModelFactory;
    GasSettingsViewModel viewModel;

    private EditText gasPriceText;
    private EditText gasLimitText;
    private TextView networkFeeText;
    private TextView gasPriceInfoText;
    private TextView gasLimitInfoText;
    private SeekBar gasPriceSlider;
    private SeekBar gasLimitSlider;

    private BigInteger gasLimitMin;
    private BigInteger gasLimitMax;
    private Integer gasPriceMinGwei;
    private Integer gasPriceMaxGwei;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gas_settings);
        toolbar();

        gasPriceSlider = findViewById(R.id.gas_price_slider);
        gasLimitSlider = findViewById(R.id.gas_limit_slider);
        gasPriceText = findViewById(R.id.gas_price_text);
        gasLimitText = findViewById(R.id.gas_limit_text);
        networkFeeText = findViewById(R.id.text_network_fee);
        gasPriceInfoText = findViewById(R.id.gas_price_info_text);
        gasLimitInfoText = findViewById(R.id.gas_limit_info_text);

        gasPriceSlider.setPadding(0, 0, 0, 0);
        gasLimitSlider.setPadding(0, 0, 0, 0);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(GasSettingsViewModel.class);

        BigInteger gasPrice = new BigInteger(getIntent().getStringExtra(C.EXTRA_GAS_PRICE));
        BigInteger gasLimit = new BigInteger(getIntent().getStringExtra(C.EXTRA_GAS_LIMIT));
        gasLimitMin = BigInteger.valueOf(C.GAS_LIMIT_MIN);
        gasLimitMax = BigInteger.valueOf(C.GAS_LIMIT_MAX);
        BigInteger gasPriceMin = BigInteger.valueOf(C.GAS_PRICE_MIN);
        BigInteger networkFeeMax = BigInteger.valueOf(C.NETWORK_FEE_MAX);

        gasPriceMinGwei = BalanceUtils.weiToGweiBI(gasPriceMin).intValue();
        gasPriceMaxGwei = BalanceUtils
                .weiToGweiBI(networkFeeMax.divide(gasLimitMax))
                .subtract(BigDecimal.valueOf(gasPriceMinGwei))
                .intValue();
        gasPriceSlider.setMax(gasPriceMaxGwei);
        int gasPriceProgress = BalanceUtils
                .weiToGweiBI(gasPrice)
                .subtract(BigDecimal.valueOf(gasPriceMinGwei))
                .intValue();
        gasPriceSlider.setProgress(gasPriceProgress);
        gasPriceSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            gasPriceText.clearFocus();
                        }
                        viewModel.gasPrice().setValue(BalanceUtils.gweiToWei(BigDecimal.valueOf(progress + gasPriceMinGwei)));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        gasLimitSlider.setMax(gasLimitMax.subtract(gasLimitMin).intValue());
        gasLimitSlider.setProgress(gasLimit.subtract(gasLimitMin).intValue());
        gasLimitSlider.refreshDrawableState();
        gasLimitSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            gasLimitText.clearFocus();
                        }
                        progress = progress / 100;
                        progress = progress * 100;
                        viewModel.gasLimit().setValue(BigInteger.valueOf(progress).add(gasLimitMin));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        gasPriceText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                gasPriceText.setError(null);
                try {
                    Integer price = Integer.parseInt(gasPriceText.getText().toString());
                    if (gasPriceText.hasFocus()) {
                        gasPriceSlider.setProgress(price - gasPriceMinGwei);
                    }
                } catch (Exception e) { }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        gasLimitText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                gasLimitText.setError(null);
                try {
                    if (gasLimitText.hasFocus()) {
                        Integer limit = Integer.parseInt(gasLimitText.getText().toString());
                        gasLimitSlider.setProgress(limit -  gasLimitMin.intValue());
                    }
                } catch (Exception e) { }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        viewModel.gasPrice().observe(this, this::onGasPrice);
        viewModel.gasLimit().observe(this, this::onGasLimit);
        viewModel.defaultNetwork().observe(this, this::onDefaultNetwork);

        viewModel.gasPrice().setValue(gasPrice);
        viewModel.gasLimit().setValue(gasLimit);
    }

    @Override
    public void onResume() {

        super.onResume();

        viewModel.prepare();
    }

    private void onDefaultNetwork(NetworkInfo network) {
        gasPriceInfoText.setText(getString(R.string.info_gas_price).replace(C.ETHEREUM_NETWORK_NAME, network.name));
        gasLimitInfoText.setText(getString(R.string.info_gas_limit).replace(C.ETHEREUM_NETWORK_NAME, network.symbol));
    }

    private void onGasPrice(BigInteger price) {
        String priceStr = BalanceUtils.weiToGwei(new BigDecimal(price));

        if (!gasPriceText.hasFocus()) {
            gasPriceText.setText(priceStr);
        }

        updateNetworkFee();
    }

    private void onGasLimit(BigInteger limit) {
        if (!gasLimitText.hasFocus()) {
            gasLimitText.setText(limit.toString());
        }

        updateNetworkFee();
    }

    private void updateNetworkFee() {
        String fee = BalanceUtils.weiToEth(viewModel.networkFee()).toPlainString() + " " + C.ETH_SYMBOL;
        networkFeeText.setText(fee);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private boolean validateFields() {
        boolean valid = true;
        Integer price = Integer.parseInt(gasPriceText.getText().toString());
        if (price < gasPriceMinGwei) {
            gasPriceText.setError(getString(R.string.too_low));
            valid = false;
        }
        if (price > gasPriceMaxGwei) {
            gasPriceText.setError(getString(R.string.too_high));
            valid = false;
        }
        Integer limit = Integer.parseInt(gasLimitText.getText().toString());
        if (limit < gasLimitMin.intValue()) {
            gasLimitText.setError(getString(R.string.too_low));
            valid = false;
        }
        if (limit > gasLimitMax.intValue()) {
            gasLimitText.setError(getString(R.string.too_high));
            valid = false;
        }
        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: {
                if (!validateFields()) {
                    return super.onOptionsItemSelected(item);
                }
                Intent intent = new Intent();
                intent.putExtra(C.EXTRA_GAS_SETTINGS, new GasSettings(
                        new BigDecimal(viewModel.gasPrice().getValue()),
                        new BigDecimal(viewModel.gasLimit().getValue())));
                setResult(RESULT_OK, intent);
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
