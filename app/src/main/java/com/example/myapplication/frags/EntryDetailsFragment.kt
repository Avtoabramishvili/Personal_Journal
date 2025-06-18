package com.example.myapplication.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentEntryDetailsBinding
import com.example.myapplication.viewmodel.JournalViewModel
import com.example.myapplication.viewmodel.JournalViewModelFactory
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.room.JournalEntry
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class EntryDetailsFragment : Fragment() {
    private var _binding: FragmentEntryDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: JournalViewModel
    private var currentJournalEntry: JournalEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = JournalViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(JournalViewModel::class.java)

        val toolbar: Toolbar = binding.entryDetailsToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "რედაქტირება"

        val args = EntryDetailsFragmentArgs.fromBundle(requireArguments())
        val entryId = args.entryId
        viewModel.getEntryById(entryId).observe(viewLifecycleOwner) { entry ->
            if (_binding == null) {
                Log.w("EntryDetailsFragment", "Binding is null, view destroyed. Skipping UI update.")
                return@observe
            }
            entry?.let {
                currentJournalEntry = it
                binding.titleEditText.setText(it.title)
                binding.contentEditText.setText(it.content)
                when (it.mood.lowercase()) { // Use lowercase for comparison
                    "happy" -> binding.moodRadioGroup.check(R.id.happyRadio)
                    "sad" -> binding.moodRadioGroup.check(R.id.sadRadio)
                    else -> binding.moodRadioGroup.check(R.id.neutralRadio)
                }
            } ?: run {
                Toast.makeText(requireContext(), "ჩანაწერი ვერ იძებნება, ან წაიშალა", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
        binding.saveButton.setOnClickListener {
            saveEntry()
        }
        binding.deleteButton.setOnClickListener {
            deleteEntry()
        }
    }

    private fun saveEntry() {
        if (_binding == null) {
            Log.w("EntryDetailsFragment", "Binding is null, view destroyed. Skipping save.")
            Toast.makeText(requireContext(), "Error: View not available to save changes.", Toast.LENGTH_SHORT).show()
            return
        }
        currentJournalEntry?.let { existingEntry ->
            val title = binding.titleEditText.text.toString().trim()
            val content = binding.contentEditText.text.toString().trim()
            val selectedMood = when (binding.moodRadioGroup.checkedRadioButtonId) {
                R.id.happyRadio -> "Happy"
                R.id.sadRadio -> "Sad"
                else -> "Neutral"
            }
            if (title.isEmpty()) {
                binding.titleEditText.error = "სათაური აუცილებელია"
                return
            }
            val updatedEntry = existingEntry.copy(
                title = title,
                content = content,
                mood = selectedMood
            )
            viewModel.updateEntry(updatedEntry)
            Toast.makeText(requireContext(), "ჩანაწერი განახლებულია!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } ?: run {
            Toast.makeText(requireContext(), "ვერ მოიძებნა", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEntry() {
        if (_binding == null) {
            Log.w("EntryDetailsFragment", "Binding is null, view destroyed. Skipping delete.")
            Toast.makeText(requireContext(), "Error: View not available to delete entry.", Toast.LENGTH_SHORT).show()
            return
        }
        currentJournalEntry?.let { entryToDelete ->
            viewModel.deleteEntry(entryToDelete)
            Toast.makeText(requireContext(), "ჩანაწერი წაშლილია", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } ?: run {
            Toast.makeText(requireContext(), "ვერ მოიძებნა", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
