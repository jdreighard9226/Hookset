document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('#catchesTable')) {
        new DataTable('#catchesTable', {
            pageLength: 20,
            lengthMenu: [10, 20, 50, 100],
            autoWidth: false,
            order: [[2, 'desc']],
            columnDefs: [
                { targets: 0, width: '30%' },
                { targets: 1, width: '30%' },
                { targets: 2, width: '20%' },
                { targets: 3, width: '20%', orderable: false }
            ]
        });
    }
});