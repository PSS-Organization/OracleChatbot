import { FilterList, Add } from "@mui/icons-material"; // Nuevos íconos de MUI

const Header = () => {
    return (
        <div className="flex items-center justify-between bg-white p-4 shadow-md">
            {/* Título y subtítulo */}
            <div>
                <h1 className="text-xl font-bold">Proyecto PSS - Oracle</h1>
                <p className="text-gray-500 text-sm">12 tasks in progress</p>
            </div>

            {/* Botones */}
            <div className="flex space-x-2">
                <button className="flex items-center space-x-1 bg-gray-100 px-3 py-2 rounded-md">
                    <FilterList />
                    <span>Filter</span>
                </button>

                <button className="flex items-center space-x-1 bg-blue-500 text-white px-4 py-2 rounded-md">
                    <Add />
                    <span>Add Task</span>
                </button>
            </div>
        </div>
    );
};

export default Header;
