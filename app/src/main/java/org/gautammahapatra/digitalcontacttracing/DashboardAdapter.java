package org.gautammahapatra.digitalcontacttracing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    private Context context;
    private List<DashboardDataBinder> dashboardDataBinderList;

    DashboardAdapter(Context context, List<DashboardDataBinder> dashboardDataBinderList) {
        this.context = context;
        this.dashboardDataBinderList = dashboardDataBinderList;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_layout, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        holder.signalStrength.setText(dashboardDataBinderList.get(position).getSignalStrength());
        holder.deviceName.setText(dashboardDataBinderList.get(position).getDeviceName());
        holder.distance.setText(dashboardDataBinderList.get(position).getDistance());
    }

    @Override
    public int getItemCount() {
        return dashboardDataBinderList.size();
    }

    class DashboardViewHolder extends RecyclerView.ViewHolder {
        TextView signalStrength, deviceName, distance;

        DashboardViewHolder(@NonNull View itemView) {
            super(itemView);
            signalStrength = itemView.findViewById(R.id.signal_strength);
            deviceName = itemView.findViewById(R.id.device_name);
            distance = itemView.findViewById(R.id.distance);
        }
    }


}

class DashboardDataBinder {
    private String signalStrength, distance, deviceName;

    DashboardDataBinder(int signalStrength, String deviceName) {
        double d0 = 1;
        double eta = 2;
        double K_i = 1;
        double Pl_d0 = -55;
        double Pt = 0;
        setDistance(String.valueOf(Math.pow(d0 * 10., ((Pt - Pl_d0 + signalStrength) / (10 * eta * K_i)))));
        setSignalStrength(String.valueOf(signalStrength));
        setDeviceName(deviceName);
    }

    String getSignalStrength() {
        return signalStrength;
    }

    private void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    String getDistance() {
        return distance;
    }

    private void setDistance(String distance) {
        this.distance = distance;
    }

    String getDeviceName() {
        return deviceName;
    }

    private void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "DashboardDataBinder{" +
                "signalStrength='" + signalStrength + '\'' +
                ", distance='" + distance + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
