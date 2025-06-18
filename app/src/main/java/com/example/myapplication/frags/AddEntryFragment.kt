package com.example.myapplication.frags

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAddEntryBinding
import com.example.myapplication.room.JournalEntry
import com.example.myapplication.util.DateUtils
import com.example.myapplication.viewmodel.JournalViewModel
import com.example.myapplication.viewmodel.JournalViewModelFactory
import java.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class AddEntryFragment : Fragment() {
    private var _binding: FragmentAddEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: JournalViewModel
    private var selectedDate: Calendar = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = JournalViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(JournalViewModel::class.java)
        val toolbar: Toolbar = binding.addEntryToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "ჩანაწერის დამატება"
        binding.datePickerButton.text = DateUtils.formatDate(selectedDate.time)
        binding.datePickerButton.setOnClickListener {
            showDatePicker()
        }
        binding.saveButton.setOnClickListener {
            saveEntry()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("AddEntryFragment", "onDestroyView called, setting _binding to null.")
        _binding = null
    }
    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                binding.datePickerButton.text = DateUtils.formatDate(selectedDate.time)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
    private fun saveEntry() {
        if (_binding == null) {
            Log.w("AddEntryFragment", "saveEntry: _binding is null, view destroyed. Skipping save.")
            Toast.makeText(requireContext(), "Error: View not available to save entry.", Toast.LENGTH_SHORT).show()
            return
        }
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()
        val mood = when (binding.moodRadioGroup.checkedRadioButtonId) {
            R.id.happyRadio -> "Happy"
            R.id.neutralRadio -> "Neutral"
            R.id.sadRadio -> "Sad"
            else -> "Neutral"
        }
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(requireContext(), "სათაური და კონტენტი არ უნდა იყოს ცარიელი", Toast.LENGTH_SHORT).show()
            return
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "მომხმარებელი არაა ავტორიზირებული.", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val newEntry = JournalEntry(
            title = title,
            content = content,
            date = selectedDate.time.time,
            mood = mood,
            userId = userId
        )
        viewModel.addEntry(newEntry)
        Toast.makeText(requireContext(), "ჩანაწერი შენახულია", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}