package com.ftechs2016.pedometre

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ftechs2016.pedometre.AdapterClass.MyViewHolder

class AdapterClass(private var list: List<ReportModel>, var context: Context) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reports_items, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        holder.steps.text = model.steps
        holder.distance.text = model.distance
        holder.calories.text = model.calories
        holder.time.text = model.time
        holder.date.text = model.date
        // val d: String = model.date!!
        // val st = model.steps
        //        if (Objects.equals(d, "15-Jan-2023")){
//            holder.hide.setVisibility(View.GONE);
//        }
//        if (Objects.equals(st,"0.0")){
//            holder.hide.setVisibility(View.GONE);
//        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var steps: TextView
        var distance: TextView
        var calories: TextView
        var time: TextView
        var date: TextView
        var hide: RelativeLayout

        init {
            steps = itemView.findViewById(R.id.steps)
            distance = itemView.findViewById(R.id.distance)
            calories = itemView.findViewById(R.id.calories)
            time = itemView.findViewById(R.id.time)
            date = itemView.findViewById(R.id.date)
            hide = itemView.findViewById(R.id.layout_toHide)
        }
    }
}
