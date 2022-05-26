package com.example.vultureapp.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vultureapp.Models.Camera;
import com.example.vultureapp.R;
import com.example.vultureapp.Views.ListCamActivity;

public class ListCamAdapter extends RecyclerView.Adapter<ListCamAdapter.ViewHolder>{

    private ListCamActivity context;
    private Camera[] localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView labCamReference;
        private Button btnStreaming;
        private Button btnClips;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            btnClips = view.findViewById(R.id.btnClips);
            btnStreaming = view.findViewById(R.id.btnStreaming);
            labCamReference = view.findViewById(R.id.labCamReference);

        }

    }


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public ListCamAdapter(Camera[] dataSet, ListCamActivity context) {
        localDataSet = dataSet;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_cam_list, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.labCamReference.setText(localDataSet[position].getId() + " - " + localDataSet[position].getName());


        viewHolder.btnClips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.createRecListMessageDialog(localDataSet[position]);
            }
        });

        viewHolder.btnStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.goToCamActivity(localDataSet[position]);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

}
