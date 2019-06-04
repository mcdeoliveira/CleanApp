package org.emstrack.ambulance.adapters;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import org.emstrack.ambulance.R;
import org.emstrack.models.Waypoint;

public class WaypointInfoAdapter extends RecyclerView.Adapter<WaypointInfoAdapter.MyViewHolder>{

    private LayoutInflater inflater;
    private List<Waypoint> waypointList;

    public WaypointInfoAdapter(Context ctx, List<Waypoint> waypointList) {
        inflater = LayoutInflater.from(ctx);
        this.waypointList = waypointList;
    }

    @Override
    public WaypointInfoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.waypoint_buttons, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(WaypointInfoAdapter.MyViewHolder holder, int position) {
        holder.addr.setText(waypointList.get(position).getLocation().toAddress());

        int dodgerblue = 0xFF1E90FF;
        int mediumseagreen = 0xFF3CB371;
        int darkorange = 0xFFFF8C00;
        // set color of button based on visiting status
        if( waypointList.get(position).isVisiting() ){
            //blue
            holder.addr.setBackgroundColor(dodgerblue);
        } else if ( waypointList.get(position).isSkipped() ) {
            //orange
            holder.addr.setBackgroundColor(darkorange);
        } else if ( waypointList.get(position).isVisited() ) {
            //green
            holder.addr.setBackgroundColor(mediumseagreen);
        }
    }

    @Override
    public int getItemCount() {
        return waypointList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        Button addr;

        public MyViewHolder(View itemView) {
            super(itemView);
            addr = (Button) itemView.findViewById(R.id.address);
        }
    }
}
