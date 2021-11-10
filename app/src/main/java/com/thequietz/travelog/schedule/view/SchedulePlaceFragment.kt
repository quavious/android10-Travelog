package com.thequietz.travelog.schedule.view

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.thequietz.travelog.R
import com.thequietz.travelog.databinding.FragmentSchedulePlaceBinding
import com.thequietz.travelog.schedule.adapter.SchedulePlaceAdapter
import com.thequietz.travelog.schedule.adapter.SchedulePlaceSelectedAdapter
import com.thequietz.travelog.schedule.viewmodel.SchedulePlaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SchedulePlaceFragment : Fragment() {
    private var _binding: FragmentSchedulePlaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var schedulePlaceAdapter: SchedulePlaceAdapter
    private lateinit var schedulePlaceSelectedAdapter: SchedulePlaceSelectedAdapter
    private lateinit var mContext: Context
    private lateinit var gson: Gson

    private val viewModel: SchedulePlaceViewModel by viewModels()

    private fun Button.setEnable() {
        this.isEnabled = true
        this.isClickable = true
        this.alpha = 1F
    }

    private fun Button.setDisable() {
        this.isEnabled = false
        this.isClickable = false
        this.alpha = 0.4F
    }

    private fun View.handleClick() {
        val view = this
        lifecycleScope.launch {
            view.alpha = 0.8F
            delay(400)
            view.alpha = 0.4F
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_place, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = context as Context
        gson = Gson()

        binding.etSearch.setOnKeyListener { v, code, _ ->
            if (code !== KeyEvent.KEYCODE_ENTER) return@setOnKeyListener false

            val keyword = (v as EditText).text.toString()
            if (keyword.isEmpty()) {
                viewModel.loadPlaceList()
            } else {
                viewModel.searchPlaceList(keyword)
            }
            true
        }

        binding.btnSelectDate.setOnClickListener {
            val placeList = viewModel.placeSelectedList.value
            val placeString = gson.toJson(placeList)
            val action =
                SchedulePlaceFragmentDirections.actionSchedulePlaceFragmentToScheduleSelectFragment(
                    placeString
                )
            it.findNavController().navigate(action)
        }

        binding.btnSearchPlace.setOnClickListener {
            val action = SchedulePlaceFragmentDirections.actionSchedulePlaceFragmentToPlaceSearchFragment()
            it.findNavController().navigate(action)
        }

        viewModel.placeList.observe(viewLifecycleOwner, {

            binding.tvEmptyResult.visibility = if (it.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            schedulePlaceAdapter = SchedulePlaceAdapter(
                it,
                object : SchedulePlaceAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {

                        view.handleClick()
                        viewModel.addPlaceSelectedList(position, it[position])
                    }
                }
            )
            binding.rvSelectSearch.layoutManager = GridLayoutManager(mContext, 2)
            binding.rvSelectSearch.adapter = schedulePlaceAdapter
        })

        viewModel.placeSelectedList.observe(viewLifecycleOwner, {
            if (it.size == 0) {
                binding.btnSelectDate.setDisable()
            } else {
                binding.btnSelectDate.setEnable()
            }

            schedulePlaceSelectedAdapter = SchedulePlaceSelectedAdapter(
                it,
                object : SchedulePlaceSelectedAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        viewModel.removePlaceSelectedList(position)
                    }
                }
            )
            binding.rvSelectItem.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            binding.rvSelectItem.adapter = schedulePlaceSelectedAdapter
        })

        viewModel.loadPlaceList()
        viewModel.initPlaceSelectedList()
    }
}
