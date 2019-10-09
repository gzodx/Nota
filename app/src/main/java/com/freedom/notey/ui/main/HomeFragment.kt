
package com.freedom.notey.ui.main


import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.freedom.notey.R
import com.freedom.notey.databinding.FragmentHomeBinding
import com.freedom.notey.db.Note
import com.freedom.notey.ui.NoteViewModel
import com.freedom.notey.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs


class HomeFragment : MainNavigationFragment() {

    private val noteViewModel: NoteViewModel by viewModel()
    private lateinit var  viewModel: NoteViewModel
    private lateinit var adapter:NoteAdapter
    private var lists: ArrayList<Note> = ArrayList()
    private lateinit var binding:FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)

        viewModel=ViewModelProvider(this).get(noteViewModel::class.java)

        setupViews()

        setupRecylerView()

        subscribeObserver()

        return binding.root
    }


    private fun subscribeObserver(){

        viewModel.loadData().observe(viewLifecycleOwner, Observer {
            it.let {
                lists.addAll(it)

                val newlist=ArrayList<Note>()

                newlist.addAll(it)

                adapter.submitList(newlist)

                emptylist()
            }


        })
    }

    private fun setupRecylerView(){
        adapter= NoteAdapter(NoteClickLitener {
            val action=HomeFragmentDirections.actionHomeFragmentToAddNote()
            action.note=it
            findNavController().navigate(action)
        })
        binding.recyclerView.apply {
            layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            adapter=this@HomeFragment.adapter
        }


        ItemTouchHelper(object : SimpleCallback(
            0,
            LEFT or RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val note=adapter.currentList[viewHolder.adapterPosition]

                val position=viewHolder.layoutPosition

                lists.removeAt(position)

                viewModel.Delete(note)

                val newlist=ArrayList<Note>()

                newlist.addAll(lists)
                adapter.submitList(newlist)
                root.snack("Moved To Trash"){
                    action("Undo"){

                        viewModel.insertNote(note)

                        lists.add(position,note)

                        val newlists=ArrayList<Note>()

                        newlists.addAll(lists)

                        adapter.submitList(newlists)

                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean

            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                when {
                    dX>0 -> {
                        val  alpha=1- abs(dX) /viewHolder.itemView.width
                        viewHolder.itemView.setBackgroundResource(R.drawable.line)
                        viewHolder.itemView.alpha=alpha
                        viewHolder.itemView.translationX=dX
                    }
                    dX<0 -> {
                        val  alpha=1- abs(dX) /viewHolder.itemView.width
                        viewHolder.itemView.setBackgroundResource(R.drawable.line)
                        viewHolder.itemView.alpha=alpha
                        viewHolder.itemView.translationX=dX
                    }

                }
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun setupViews(){
        setHasOptionsMenu(true)
        binding.btnAdd.setOnClickListener {
            val action= HomeFragmentDirections.actionHomeFragmentToAddNote()
            Navigation.findNavController(it).navigate(action)
        }
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { (activity as AppCompatActivity).drawerlayout.openDrawer(
            GravityCompat.START) }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.changeview -> {
                if (binding.recyclerView.layoutManager is LinearLayoutManager){

                    item.icon=resources.getDrawable(R.drawable.ic_outline_view,null)
                    binding.recyclerView.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

                }else{
                    item.icon=resources.getDrawable(R.drawable.ic_grid,null)
                    binding.recyclerView.layoutManager=LinearLayoutManager(context)
                }


            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun emptylist(){
        if (adapter.itemCount == 0||adapter.itemCount<0){
            binding.emptystate.visibility=VISIBLE
        }else{
            binding.emptystate.visibility=GONE
        }
    }


}
