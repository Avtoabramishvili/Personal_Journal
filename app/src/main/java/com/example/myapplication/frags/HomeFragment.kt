package com.example.myapplication.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.JournalEntryAdapter
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.viewmodel.JournalViewModel
import com.example.myapplication.viewmodel.JournalViewModelFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: JournalEntryAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = JournalViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(JournalViewModel::class.java)
        val toolbar: Toolbar = binding.homeToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "ჩემი ჩანაწერები"
        setupRecyclerView()
        observeEntries()
    }
    private fun setupRecyclerView() {
        adapter = JournalEntryAdapter { entry ->
            val direction = HomeFragmentDirections.actionHomeToEntryDetail(entry.id)
            findNavController().navigate(direction)
        }
        binding.entriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
            itemAnimator = null
        }
    }
    private fun observeEntries() {
        viewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            binding.emptyState.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}