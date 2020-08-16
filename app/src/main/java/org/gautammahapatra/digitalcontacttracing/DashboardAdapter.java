package org.gautammahapatra.digitalcontacttracing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>
{

    private Context context;
    private List<DashboardDataBinder> dashboardDataBinderList;

    public DashboardAdapter(Context context, List<DashboardDataBinder> dashboardDataBinderList)
    {
        this.context = context;
        this.dashboardDataBinderList = dashboardDataBinderList;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_layout, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position)
    {
        holder.signalStrength.setText(dashboardDataBinderList.get(position).getSignalStrength());
        holder.deviceType.setText(dashboardDataBinderList.get(position).getDeviceType());
        holder.deviceName.setText(dashboardDataBinderList.get(position).getDeviceName());
    }

    @Override
    public int getItemCount()
    {
        return dashboardDataBinderList.size();
    }

    public class DashboardViewHolder extends RecyclerView.ViewHolder
    {
        TextView signalStrength, deviceType, deviceName;

        public DashboardViewHolder(@NonNull View itemView)
        {
            super(itemView);
            signalStrength = itemView.findViewById(R.id.signal_strength);
            deviceType = itemView.findViewById(R.id.device_type);
            deviceName = itemView.findViewById(R.id.device_name);
        }
    }


}

class DashboardDataBinder
{
    private String signalStrength, deviceType, deviceName;

    public DashboardDataBinder(String signalStrength, String deviceType, String deviceName)
    {
        this.signalStrength = signalStrength;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
    }

    public String getSignalStrength()
    {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength)
    {
        this.signalStrength = signalStrength;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }
}
