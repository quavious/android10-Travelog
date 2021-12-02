package com.thequietz.travelog.record.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thequietz.travelog.data.RecordRepository
import com.thequietz.travelog.record.model.RecordImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecordViewManyInnerViewModel @Inject constructor(
    val repository: RecordRepository
) : ViewModel() {

    private val _deleteState = MutableLiveData<Boolean>()
    val deleteState: LiveData<Boolean> = _deleteState

    private val _checkedList = MutableLiveData<List<Int>>()
    val checkedList: LiveData<List<Int>> = _checkedList

    var list = mutableListOf<RecordImage>()
    init {
        _deleteState.value = false
        _checkedList.value = mutableListOf()
        loadList()
    }
    fun loadList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                list = repository.loadRecordImagesByTravelId(RecordViewOneViewModel.currentTravleId).toMutableList()
            }
        }
    }
/*    fun findInd(item: RecordImage): Int {
        var ind = 0
        list.forEachIndexed { idx, it ->
            if (it.id == item.id) {
                ind = idx
                return@forEachIndexed
            }
        }
        return ind
    }*/
    fun changeDeleteState() {
        viewModelScope.launch {
            deleteState.value?.let {
                _deleteState.value = !it
            }
        }
    }
    fun addCheck(id: Int) {
        viewModelScope.launch {
            checkedList.value?.let {
                val res = it.toMutableSet()
                res.add(id)
                _checkedList.value = res.toMutableList().sorted()
            }
        }
    }
    fun deleteCheck(id: Int) {
        viewModelScope.launch {
            checkedList.value?.let {
                val res = it.toMutableList()
                var ind = -1
                res.forEachIndexed { idx, it ->
                    if (id == it) {
                        ind = idx
                        return@forEachIndexed
                    }
                }
                if (ind != -1) {
                    res.removeAt(ind)
                    res.sort()
                }
                _checkedList.value = res
            }
        }
    }
    fun deleteChecked() {
        viewModelScope.launch {
            checkedList.value?.let { list ->
                list.forEach {
                    repository.deleteNewRecordImage(it)
                }
            }
            clearChecked()
            println("delete end")
        }
    }
    fun findChecked(id: Int): Boolean {
        var flag = false
        checkedList.value?.let { list ->
            list.forEach {
                if (it == id) {
                    flag = true
                    return@forEach
                }
            }
        }
        return flag
    }
    fun clearChecked() {
        viewModelScope.launch {
            _checkedList.value = mutableListOf()
        }
    }
}
