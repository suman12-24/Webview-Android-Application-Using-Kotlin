package wekarthub.inc.sudhritifooddeliveryhotel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import  wekarthub.inc.sudhritifooddeliveryhotel.databinding.AdapterChooseMediaBinding


class ChooseMediaAdapter constructor(
    private val titleArrayList: Array<String>,
    private val onChooseMediaClick: (Int) -> Unit = { _ -> }
) : RecyclerView.Adapter<ChooseMediaAdapter.ChooseMediaViewHolder>() {


    inner class ChooseMediaViewHolder(private val binding: AdapterChooseMediaBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {

        fun bind(position: Int) {
            binding.textViewTitle.text = titleArrayList[position]
            binding.linearLayoutChooseMedia.setOnClickListener {
                onChooseMediaClick(position)
            }

        }

    }


    override fun onCreateViewHolder(

        parent: ViewGroup,
        viewType: Int
    ): ChooseMediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterChooseMediaBinding =
            AdapterChooseMediaBinding.inflate(inflater, parent, false)
        return ChooseMediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChooseMediaViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return titleArrayList.size
    }
}