document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('#fliesTable')) {
        new DataTable('#fliesTable', {
            pageLength: 20,
            lengthMenu: [10, 20, 50, 100],
            autoWidth: false,
            order: [[1, 'asc']],
            columnDefs: [
                { targets: 0, width: '15%', orderable: false },
                { targets: 1, width: '40%' },
                { targets: 2, width: '25%' },
                { targets: 3, width: '20%', orderable: false }
            ]
        });
    }
});