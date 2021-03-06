package br.com.cadim.cadim.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import br.com.cadim.cadim.Controller.Api;
import br.com.cadim.cadim.Controller.DiagnosticController;
import br.com.cadim.cadim.Controller.FoundDevices;
import br.com.cadim.cadim.Controller.PerformNetworkRequest;
import br.com.cadim.cadim.Model.Diagnostico;
import br.com.cadim.cadim.Model.Ecg;
import br.com.cadim.cadim.Model.Paciente;
import br.com.cadim.cadim.R;

import static br.com.cadim.cadim.Controller.PerformNetworkRequest.CODE_POST_REQUEST;

public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SELECT_DISCOVERED_DEVICE = 3;
    private Paciente paciente;
    public Diagnostico diagnosticoEcg;
    ListView ecgTodayList;
    ArrayList<Ecg> ecgs;
    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();

        paciente = getIntent().getExtras().getParcelable("paciente");
        ecgs = getIntent().getParcelableArrayListExtra("listaEcg");

        if (ecgs != null) {
            setContentView(R.layout.today_exam);
            ecgTodayList = (ListView) findViewById(R.id.ecgsToday);
            CustomTodayExam cte = new CustomTodayExam();


            ecgTodayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Ecg ecg = (Ecg) ecgTodayList.getAdapter()
                            .getItem(position);
                    menuExam(ecg);
                }
            });

            ecgTodayList.setAdapter(cte);
        } else {
            setContentView(R.layout.home_screen);
        }
        FloatingActionButton btnAddExame = (FloatingActionButton) findViewById(R.id.addExame);

        ImageButton btnExame = (ImageButton) findViewById(R.id.buttonExame);
        ImageButton btnDiagnostico = (ImageButton) findViewById(R.id.buttonDiagnosticos);
        ImageButton btnSettings = (ImageButton) findViewById(R.id.buttonSettings);
        ImageButton btnHistoric = (ImageButton) findViewById(R.id.buttonHistoric);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();

        btnHistoric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent carregarHistorico = new Intent(HomeActivity.this,
                        LoadHistoricActivity.class);
                carregarHistorico.putExtra("paciente", paciente);
                carregarHistorico.putParcelableArrayListExtra("listaEcg", ecgs);
                startActivity(carregarHistorico);
            }
        });

        btnAddExame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchDevicesIntent = new Intent(HomeActivity.this,
                        FoundDevices.class);
                searchDevicesIntent.putExtra("paciente", paciente);
                searchDevicesIntent.putParcelableArrayListExtra("listaEcg", ecgs);
                startActivityForResult(searchDevicesIntent, SELECT_DISCOVERED_DEVICE);
            }
        });

        btnDiagnostico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent carregarDiagnosticos = new Intent(HomeActivity.this,
                        LoadDiagnosticActivity.class);
                carregarDiagnosticos.putExtra("paciente", paciente);
                carregarDiagnosticos.putParcelableArrayListExtra("listaEcg", ecgs);
                startActivity(carregarDiagnosticos);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Settings");
            }
        });
    }

    private void menuExam(final Ecg ecgSelected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione").
                setItems(R.array.ecg_menu_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showExam(ecgSelected);
                                break;
                            case 1:
                                try {
                                    showDiagnotic(ecgSelected);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 2:
                                showInformation(ecgSelected);
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void showExam(Ecg ecg) {
        Intent showExamIntent = new Intent(HomeActivity.this, ReadEcgActivity.class);
        showExamIntent.putExtra("ecg", ecg);
        startActivity(showExamIntent);
    }

    private void showDiagnotic(Ecg ecg) throws ExecutionException, InterruptedException, JSONException {
        Diagnostico diagnostico = diagnosticCheck(ecg.getEcgId());

        if (diagnostico != null) {
            Intent diagnosticIntent = new Intent(HomeActivity.this, DiagnosticActivity.class);
            diagnosticIntent.putExtra("diagnostic", diagnostico);
            startActivity(diagnosticIntent);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Ainda não há dianósticos para esse exame",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showInformation(Ecg ecg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Informações");
        builder.setCancelable(true);
        builder.setMessage(ecg.getDataHora() + "\n" + "67 bpm" + "\n" + "15 s");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Ao clicar, o AlertDialog fechará
            }
        });
        builder.show();
    }


    private void checkBluetooth() {

        if (bluetoothAdapter == null) {
            System.out.println("Hardware Bluetooth não suportado.");
        } else {
            if (bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {
                    System.out.println("O Bluetooth está atualmente no processo de descoberta do dispositivo.");
                } else {
                    System.out.println("Bluetooth ativado.");
                }
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                System.out.println("Solicitando permissão do Bluetooth");
            }
        }
    }

    private Diagnostico diagnosticCheck(int ecgId) throws ExecutionException, InterruptedException, JSONException {
        HashMap<String, String> params = new HashMap<>();
        params.put("ecgId", String.valueOf(ecgId));

        PerformNetworkRequest request = new PerformNetworkRequest(
                Api.URL_DIAGNOSTIC_ECG_LIST, params, CODE_POST_REQUEST);

        JSONObject object = new JSONObject(request.execute().get());
        return DiagnosticController.returnDiagnostic(object);
    }

    private class CustomTodayExam extends BaseAdapter {

        @Override
        public int getCount() {
            return ecgs.size();
        }

        @Override
        public Ecg getItem(int i) {
            return ecgs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.custom_exam_list, null);

            TextView dataHoraEcg = (TextView) view.findViewById(R.id.data_hora);

            dataHoraEcg.setText(ecgs.get(i).getDataHora());
            return view;
        }
    }
}
