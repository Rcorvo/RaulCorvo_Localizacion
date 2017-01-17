package com.raulcorvo.localizacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private static final String[] PERMISOS_LOCALIZACION = {
            Manifest.permission.ACCESS_FINE_LOCATION};

    private static final int PETICION_LOCALIZACION = 123;

    private static final long TIEMPO_MIN = 10 * 1000;
    private static final long DISTANCIA_MIN = 5;

    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio", "temporalmente no disponible", "disponible"};

    private LocationManager manejador;
    private String proveedor;
    private TextView salida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        salida = (TextView) findViewById(R.id.salida);

        if (!hayPermisoLocalizacion()) {
            ActivityCompat.requestPermissions(this, PERMISOS_LOCALIZACION, PETICION_LOCALIZACION);
        }
        else{
            init();
        }

    }

    private void init(){
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);

        log("Proveedores de localización: \n");

        muestraProveedores();

        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);

        proveedor = manejador.getBestProvider(criterio, true);

        log("Mejor proveedor: " + proveedor + "\n");
        log("Comenzamos con la última localización conocida:");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN, this);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            manejador.removeUpdates(this);
        }
    }

    private boolean hayPermiso(String perm) {
        return (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hayPermisoLocalizacion() {
        return(hayPermiso(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private void error() {
        Toast.makeText(this, "Permisos de localización denegados", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PETICION_LOCALIZACION:
                if(hayPermisoLocalizacion()){
                    init();
                }else{
                    error();
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        log("Nueva localización: ");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("Cambia estado proveedor: " + proveedor + ", estadp=" + E[Math.max(0, status)]
                + ", extras=" + extras + "\n");
    }

    @Override
    public void onProviderEnabled(String provider) {
        log("Proveedor habilitado: " + proveedor + "\n");
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("Proveedor deshabilitado: " + proveedor + "\n");
    }

    //INFORMACION
    private void log(String cadena) {
        salida.append(cadena + "\n");
    }

    private void muestraLocalizacion(Location localizacion) {
        if (localizacion == null){
            log("Localización desconocida\n");
        }
        else {
            log(localizacion.toString() + "\n");
        }
    }

    private void muestraProveedores() {
        log("Proveedor de localización: \n");

        List<String> proveedores = manejador.getAllProviders();

        for (String proveedor : proveedores) {
            muestraProveedor(proveedor);
        }
    }

    private void muestraProveedor(String proveedor) {
        LocationProvider info = manejador.getProvider(proveedor);

        log("LocationProvider[ " + "getName=" + info.getName() + ", isProviderEnabled="
                + manejador.isProviderEnabled(proveedor)+", getAccuracy="
                + A[Math.max(0, info.getAccuracy())]+ ", getPowerRequirement="
                + P[Math.max(0, info.getPowerRequirement())]
                + ", hasMonetaryCost=" + info.hasMonetaryCost()
                + ", requiresCell=" + info.requiresCell()
                + ", requiresNetwork=" + info.requiresNetwork()
                + ", requiresSatellite=" + info.requiresSatellite()
                + ", supportsAltitude=" + info.supportsAltitude()
                + ", supportsBearing=" + info.supportsBearing()
                + ", supportsSpeed=" + info.supportsSpeed()+" ]\n");
    }
}
